package com.shopbee.product.control.service.impl;

import com.shopbee.category.model.CategoryDTO;
import com.shopbee.common.exception.ConflictDataException;
import com.shopbee.common.exception.NotFoundException;
import com.shopbee.product.boundary.client.CategoriesServiceClientAdapter;
import com.shopbee.product.control.mapper.ProductCategoryMapper;
import com.shopbee.product.control.mapper.ProductMapper;
import com.shopbee.product.control.repository.ProductCategoryRepository;
import com.shopbee.product.control.repository.ProductRepository;
import com.shopbee.product.control.service.ProductsService;
import com.shopbee.product.entity.Product;
import com.shopbee.product.entity.ProductCategory;
import com.shopbee.product.model.CreateProductRequest;
import com.shopbee.product.model.PatchProductByIdRequest;
import com.shopbee.product.model.ProductCategoryDTO;
import com.shopbee.product.model.ProductDTO;
import com.shopbee.product.model.UpdateProductByIdRequest;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.virtual.threads.VirtualThreads;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@ApplicationScoped
public class ProductsServiceImpl implements ProductsService {
    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductMapper productMapper;
    private final ProductCategoryMapper productCategoryMapper;
    private final SecurityIdentity securityIdentity;
    private final CategoriesServiceClientAdapter categoriesServiceClientAdapter;
    private final ExecutorService executorService;

    @Inject
    public ProductsServiceImpl(
            ProductRepository productRepository,
            ProductCategoryRepository productCategoryRepository,
            ProductMapper productMapper,
            ProductCategoryMapper productCategoryMapper,
            SecurityIdentity securityIdentity,
            CategoriesServiceClientAdapter categoriesServiceClientAdapter,
            @VirtualThreads ExecutorService executorService
    ) {
        this.productRepository = productRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.productMapper = productMapper;
        this.productCategoryMapper = productCategoryMapper;
        this.securityIdentity = securityIdentity;
        this.categoriesServiceClientAdapter = categoriesServiceClientAdapter;
        this.executorService = executorService;
    }

    @Override
    public List<ProductDTO> getProducts(String tenantId, Integer page, Integer size) {
        List<Product> products = productRepository.findAll(tenantId, page - 1, size);
        return productMapper.toProductDTOs(products);
    }

    @Override
    public ProductDTO getProductById(String tenantId, String productId) {
        Product product = findProductById(tenantId, productId);
        return productMapper.toProductDTO(product);
    }

    @Override
    @Transactional
    public String createProduct(String tenantId, CreateProductRequest createProductRequest) {
        validateNewSku(tenantId, createProductRequest.getSku());
        Product product = productMapper.toProduct(tenantId, createProductRequest);

        String username = securityIdentity.getPrincipal().getName();
        product.setCreatedBy(username);
        product.setUpdatedBy(username);

        productRepository.persist(product);

        return product.getId().toString();
    }

    @Override
    @Transactional
    public void updateProductById(String tenantId, String productId, UpdateProductByIdRequest updateProductByIdRequest) {
        Product product = findProductById(tenantId, productId);

        productMapper.updateProduct(updateProductByIdRequest, product);

        product.setUpdatedBy(securityIdentity.getPrincipal().getName());
    }

    @Override
    @Transactional
    public void patchProductById(String tenantId, String productId, PatchProductByIdRequest patchProductByIdRequest) {
        Product product = findProductById(tenantId, productId);

        productMapper.patchProduct(patchProductByIdRequest, product);

        product.setUpdatedBy(securityIdentity.getPrincipal().getName());
    }

    @Override
    @Transactional
    public void deleteProductById(String tenantId, String productId) {
        Product product = findProductById(tenantId, productId);
        productRepository.delete(product);
    }

    @Override
    public List<ProductCategoryDTO> getProductCategories(String tenantId, String productId, Integer page, Integer size) {
        List<ProductCategory> productCategories = productCategoryRepository.findByProductIdAndTenantId(tenantId, UUID.fromString(productId), page - 1, size);
        List<CategoryDTO> categoryDTOS = productCategories.stream()
                .map(ProductCategory::getCategoryId)
                .map(categoryId -> CompletableFuture.supplyAsync(() -> categoriesServiceClientAdapter.getCategoryById(tenantId, categoryId.toString()), executorService))
                .map(CompletableFuture::join)
                .toList();
        return productCategoryMapper.toProductCategoryDTOs(categoryDTOS);
    }

    @Override
    @Transactional
    public void addCategoryToProduct(String tenantId, String productId, String categoryId) {
        if (productCategoryRepository.existsByTenantIdAndProductIdAndCategoryId(tenantId, UUID.fromString(productId), UUID.fromString(categoryId))) {
            throw new ConflictDataException("PRODUCT_CATEGORY_EXISTS", "Product already associated with category: " + categoryId);
        }

        ProductCategory productCategory = new ProductCategory();
        productCategory.setTenantId(tenantId);
        productCategory.setProductId(UUID.fromString(productId));
        productCategory.setCategoryId(UUID.fromString(categoryId));

        productCategoryRepository.persist(productCategory);
    }

    @Override
    @Transactional
    public void removeCategoryFromProduct(String tenantId, String productId, String categoryId) {
        productCategoryRepository.deleteByTenantIdAndProductIdAndCategoryId(tenantId, UUID.fromString(productId), UUID.fromString(categoryId));
    }

    private Product findProductById(String tenantId, String productId) {
        Product product = productRepository.findById(tenantId, UUID.fromString(productId));
        if (product == null) {
            throw new NotFoundException("PRODUCT_NOT_FOUND", "Product not found: " + productId);
        }
        return product;
    }

    private void validateNewSku(String tenantId, String sku) {
        if (productRepository.existsBySku(tenantId, sku)) {
            throw new ConflictDataException("PRODUCT_SKU_EXISTS", "Product with SKU [" + sku + "] already exists.");
        }
    }
}
