package com.shopbee.category.control.mapper;

import com.shopbee.category.entity.Category;
import com.shopbee.category.entity.CategoryDTO;
import com.shopbee.category.entity.CreateCategoryRequest;
import com.shopbee.category.entity.PatchCategoryByIdRequest;
import com.shopbee.category.entity.UpdateCategoryByIdRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryMapper {

    CategoryDTO toCategoryDTO(Category category);

    List<CategoryDTO> toCategoryDTOs(List<Category> categories);

    @Mapping(target = "tenantId", source = "tenantId")
    Category toCategory(String tenantId, CreateCategoryRequest createCategoryRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.SET_TO_NULL)
    void updateCategory(UpdateCategoryByIdRequest updateCategoryByIdRequest, @MappingTarget Category category);

    @BeanMapping(nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
    void patchCategory(PatchCategoryByIdRequest patchCategoryByIdRequest, @MappingTarget Category category);

}
