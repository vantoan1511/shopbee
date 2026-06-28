package com.shopbee.product.boundary.api;

import com.shopbee.product.control.service.ProductsService;
import com.shopbee.product.model.CreateProductRequest;
import com.shopbee.product.model.PatchProductByIdRequest;
import com.shopbee.product.model.UpdateProductByIdRequest;
import com.shopbee.security.auth.RequiresPermissions;
import com.shopbee.security.auth.Permission;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;

@Authenticated
public class ProductsApiImpl implements ProductsApi {

    private final ProductsService productsService;
    private final UriInfo uriInfo;

    @Inject
    public ProductsApiImpl(ProductsService productsService, UriInfo uriInfo) {
        this.productsService = productsService;
        this.uriInfo = uriInfo;
    }

    @Override
    @RequiresPermissions({Permission.PRODUCT_VIEW})
    public Response getProducts(String tenantId, Integer page, Integer size) {
        return Response.ok(productsService.getProducts(tenantId, page, size)).build();
    }

    @Override
    @RequiresPermissions({Permission.PRODUCT_VIEW})
    public Response getProductById(String tenantId, String productId) {
        return Response.ok(productsService.getProductById(tenantId, productId)).build();
    }

    @Override
    @RequiresPermissions({Permission.PRODUCT_CREATE})
    public Response createProduct(String tenantId, CreateProductRequest createProductRequest) {
        String productId = productsService.createProduct(tenantId, createProductRequest);
        URI location = uriInfo.getAbsolutePathBuilder().path(productId).build();
        return Response.created(location).build();
    }

    @Override
    @RequiresPermissions({Permission.PRODUCT_MODIFY})
    public Response updateProductById(String tenantId, String productId, UpdateProductByIdRequest updateProductByIdRequest) {
        productsService.updateProductById(tenantId, productId, updateProductByIdRequest);
        return Response.ok().build();
    }

    @Override
    @RequiresPermissions({Permission.PRODUCT_MODIFY})
    public Response patchProductById(String tenantId, String productId, PatchProductByIdRequest patchProductByIdRequest) {
        productsService.patchProductById(tenantId, productId, patchProductByIdRequest);
        return Response.ok().build();
    }

    @Override
    @RequiresPermissions({Permission.PRODUCT_MODIFY})
    public Response deleteProductById(String tenantId, String productId) {
        productsService.deleteProductById(tenantId, productId);
        return Response.noContent().build();
    }

    @Override
    @RequiresPermissions({Permission.PRODUCT_VIEW})
    public Response getProductCategories(String tenantId, String productId, Integer page, Integer size) {
        return Response.ok(productsService.getProductCategories(tenantId, productId, page, size)).build();
    }

    @Override
    @RequiresPermissions({Permission.PRODUCT_MODIFY})
    public Response addCategoryToProduct(String tenantId, String productId, String categoryId) {
        productsService.addCategoryToProduct(tenantId, productId, categoryId);
        return Response.created(uriInfo.getAbsolutePath()).build();
    }

    @Override
    @RequiresPermissions({Permission.PRODUCT_MODIFY})
    public Response removeCategoryFromProduct(String tenantId, String productId, String categoryId) {
        productsService.removeCategoryFromProduct(tenantId, productId, categoryId);
        return Response.noContent().build();
    }
}
