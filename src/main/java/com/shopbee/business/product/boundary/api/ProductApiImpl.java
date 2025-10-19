package com.shopbee.business.product.boundary.api;

import com.shopbee.business.product.control.service.ProductService;
import com.shopbee.product.boundary.api.ProductsApi;
import com.shopbee.product.model.CreateProductRequest;
import com.shopbee.product.model.PatchProductByIdRequest;
import com.shopbee.product.model.UpdateProductByIdRequest;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;

public class ProductApiImpl implements ProductsApi {

    private final ProductService productService;
    private final UriInfo uriInfo;

    @Inject
    public ProductApiImpl(ProductService productService, UriInfo uriInfo) {
        this.productService = productService;
        this.uriInfo = uriInfo;
    }

    @Override
    public Response createProduct(String tenantId, CreateProductRequest createProductRequest) {
        String productId = productService.createProduct(tenantId, createProductRequest);
        URI location = uriInfo.getAbsolutePathBuilder().path(productId).build();
        return Response.created(location).entity(productId).build();
    }

    @Override
    public Response deleteProductById(String tenantId, String productId) {
        productService.deleteProductById(tenantId, productId);
        return Response.noContent().build();
    }

    @Override
    public Response getProductById(String tenantId, String productId) {
        return Response.ok(productService.getProductById(tenantId, productId)).build();
    }

    @Override
    public Response getProducts(String tenantId, Integer offset, Integer limit) {
        return Response.ok(productService.getProducts(tenantId, offset, limit)).build();
    }

    @Override
    public Response patchProductById(String tenantId, String productId, PatchProductByIdRequest patchProductByIdRequest) {
        productService.patchProductById(tenantId, productId, patchProductByIdRequest);
        return Response.noContent().build();
    }

    @Override
    public Response updateProductById(String tenantId, String productId, UpdateProductByIdRequest updateProductByIdRequest) {
        productService.updateProductById(tenantId, productId, updateProductByIdRequest);
        return Response.noContent().build();
    }
}
