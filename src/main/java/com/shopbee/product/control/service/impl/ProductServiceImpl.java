package com.shopbee.product.control.service.impl;

import com.shopbee.common.exception.ApiServiceException;
import com.shopbee.order.model.OrderItemDTO;
import com.shopbee.product.control.mapper.ProductMapper;
import com.shopbee.product.control.repository.ProductRepository;
import com.shopbee.product.control.service.ProductService;
import com.shopbee.product.entity.Product;
import com.shopbee.product.model.CreateProductRequest;
import com.shopbee.product.model.PatchProductByIdRequest;
import com.shopbee.product.model.ProductDTO;
import com.shopbee.product.model.UpdateProductByIdRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ProductServiceImpl implements ProductService {

    private static final Logger LOG = LoggerFactory.getLogger(ProductServiceImpl.class);
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int DEFAULT_PAGE_INDEX = 0;

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Inject
    public ProductServiceImpl(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Override
    public List<ProductDTO> getProducts(String tenantId, Integer offset, Integer limit) {
        LOG.info("Getting products with offset [{}] and limit [{}]", offset, limit);
        int page = Optional.ofNullable(offset).orElse(DEFAULT_PAGE_INDEX);
        int size = Optional.ofNullable(limit).orElse(DEFAULT_PAGE_SIZE);
        List<Product> foundProducts = productRepository.findAll(tenantId, page, size);
        return productMapper.toProducts(foundProducts);
    }

    @Override
    public ProductDTO getProductById(String tenantId, String productId) {
        LOG.info("Getting product by id [{}]", productId);
        return productMapper.toProductDTO(findProductById(tenantId, productId));
    }

    @Override
    @Transactional
    public String createProduct(String tenantId, CreateProductRequest createProductRequest) {
        LOG.info("Creating new product with SKU [{}]", createProductRequest.getSku());
        validateNewSku(tenantId, createProductRequest.getSku());
        Product product = productMapper.toProduct(tenantId, createProductRequest);
        productRepository.persist(product);
        return product.getId();
    }

    @Override
    @Transactional
    public void updateProductById(String tenantId, String productId, UpdateProductByIdRequest updateProductByIdRequest) {
        LOG.info("Updating product with id [{}]", productId);
        Product product = findProductById(tenantId, productId);
        productMapper.updateProduct(updateProductByIdRequest, product);
    }

    @Override
    @Transactional
    public void patchProductById(String tenantId, String productId, PatchProductByIdRequest patchProductByIdRequest) {
        LOG.info("Patching product with id [{}]", productId);
        Product product = findProductById(tenantId, productId);
        productMapper.patchProduct(patchProductByIdRequest, product);
    }

    @Override
    @Transactional
    public void deleteProductById(String tenantId, String productId) {
        LOG.info("Deleting product with id [{}]", productId);
        Product product = findProductById(tenantId, productId);
        productRepository.delete(product);
    }

    @Override
    @Transactional
    public void reserveStock(String tenantId, List<OrderItemDTO> items) {
        LOG.info("Reserving stock for {} items", items.size());
        for (OrderItemDTO item : items) {
            Product product = findProductById(tenantId, item.getProductId());
            int newStock = product.getStockQuantity() - item.getQuantity();
            if (newStock < 0) {
                throw ApiServiceException.conflict("Not enough stock for product [{}]. Requested: {}, Available: {}",
                        item.getProductId(), item.getQuantity(), product.getStockQuantity());
            }
            product.setStockQuantity(newStock);
        }
    }

    @Override
    @Transactional
    public void releaseStock(String tenantId, List<OrderItemDTO> items) {
        LOG.info("Releasing stock for {} items", items.size());
        for (OrderItemDTO item : items) {
            Product product = findProductById(tenantId, item.getProductId());
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
        }
    }

    private Product findProductById(String tenantId, String productId) {
        Product product = productRepository.findById(tenantId, productId);
        if (product == null) {
            LOG.warn("Product [{}] not found", productId);
            throw ApiServiceException.notFound("Product [{}] not found", productId);
        }
        return product;
    }

    private void validateNewSku(String tenantId, String sku) {
        if (productRepository.countBySku(tenantId, sku) > 0) {
            throw ApiServiceException.conflict("SKU [{}] already exists", sku);
        }
    }
}
