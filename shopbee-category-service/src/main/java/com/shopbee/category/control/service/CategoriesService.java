package com.shopbee.category.control.service;

import com.shopbee.category.entity.CategoryDTO;
import com.shopbee.category.entity.CreateCategoryRequest;
import com.shopbee.category.entity.PatchCategoryByIdRequest;
import com.shopbee.category.entity.UpdateCategoryByIdRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.DefaultValue;

import java.util.List;

public interface CategoriesService {
    List<CategoryDTO> getCategories(@NotNull @Size(max = 64) String tenantId, List<String> code, @Min(1) @DefaultValue("1") Integer page, @Min(1) @Max(100) @DefaultValue("20") Integer size);

    CategoryDTO getCategoryById(@NotNull @Size(max = 64) String tenantId, String categoryId);

    String createCategory(@NotNull @Size(max = 64) String tenantId, @Valid @NotNull CreateCategoryRequest createCategoryRequest);

    void updateCategoryById(@NotNull @Size(max = 64) String tenantId, String categoryId, @Valid @NotNull UpdateCategoryByIdRequest updateCategoryByIdRequest);

    void patchCategoryById(@NotNull @Size(max = 64) String tenantId, String categoryId, @Valid @NotNull PatchCategoryByIdRequest patchCategoryByIdRequest);

    void deleteCategoryById(@NotNull @Size(max = 64) String tenantId, String categoryId);
}
