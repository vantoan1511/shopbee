package com.shopbee.product.control.service;

import com.shopbee.product.model.CreateProductRequest;
import com.shopbee.product.model.PatchProductByIdRequest;
import com.shopbee.product.model.ProductDTO;
import com.shopbee.product.model.UpdateProductByIdRequest;

import java.util.List;

public interface ProductService {

    List<ProductDTO> getProducts(String tenantId, Integer offset, Integer limit);

    ProductDTO getProductById(String tenantId, String productId);

    String createProduct(String tenantId, CreateProductRequest createProductRequest);

    void updateProductById(String tenantId, String productId, UpdateProductByIdRequest updateProductByIdRequest);

    void patchProductById(String tenantId, String productId, PatchProductByIdRequest patchProductByIdRequest);

    void deleteProductById(String tenantId, String productId);
}
