package com.shopbee.product.control.service;

import com.shopbee.product.entity.CreateProductRequest;
import com.shopbee.product.entity.PatchProductByIdRequest;
import com.shopbee.product.entity.ProductCategoryDTO;
import com.shopbee.product.entity.ProductDTO;
import com.shopbee.product.entity.UpdateProductByIdRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.DefaultValue;

import java.util.List;

public interface ProductsService {
    List<ProductDTO> getProducts(@NotNull @Size(max = 64) String tenantId, @Min(1) @DefaultValue("1") Integer page, @Min(1) @Max(100) @DefaultValue("20") Integer size);

    ProductDTO getProductById(@NotNull @Size(max = 64) String tenantId, String productId);

    String createProduct(@NotNull @Size(max = 64) String tenantId, @Valid @NotNull CreateProductRequest createProductRequest);

    void updateProductById(@NotNull @Size(max = 64) String tenantId, String productId, @Valid @NotNull UpdateProductByIdRequest updateProductByIdRequest);

    void patchProductById(@NotNull @Size(max = 64) String tenantId, String productId, @Valid @NotNull PatchProductByIdRequest patchProductByIdRequest);

    void deleteProductById(@NotNull @Size(max = 64) String tenantId, String productId);

    List<ProductCategoryDTO> getProductCategories(@NotNull @Size(max = 64) String tenantId, String productId, @Min(1) @DefaultValue("1") Integer page, @Min(1) @Max(100) @DefaultValue("20") Integer size);

    void addCategoryToProduct(@NotNull @Size(max = 64) String tenantId, String productId, String categoryId);

    void removeCategoryFromProduct(@NotNull @Size(max = 64) String tenantId, String productId, String categoryId);
}
