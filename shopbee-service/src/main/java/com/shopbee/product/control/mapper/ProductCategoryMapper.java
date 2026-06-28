package com.shopbee.product.control.mapper;

import com.shopbee.category.model.CategoryDTO;
import com.shopbee.product.model.ProductCategoryDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductCategoryMapper {

    ProductCategoryDTO toProductCategoryDTO(CategoryDTO productCategory);

    List<ProductCategoryDTO> toProductCategoryDTOs(List<CategoryDTO> productCategories);
}
