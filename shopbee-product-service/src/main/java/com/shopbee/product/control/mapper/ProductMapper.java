package com.shopbee.product.control.mapper;

import com.shopbee.product.entity.CreateProductRequest;
import com.shopbee.product.entity.PatchProductByIdRequest;
import com.shopbee.product.entity.Product;
import com.shopbee.product.entity.ProductDTO;
import com.shopbee.product.entity.UpdateProductByIdRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

    ProductDTO toProductDTO(Product product);

    List<ProductDTO> toProductDTOs(List<Product> products);

    @Mapping(target = "tenantId", source = "tenantId")
    Product toProduct(String tenantId, CreateProductRequest createProductRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void patchProduct(PatchProductByIdRequest patchProductByIdRequest, @MappingTarget Product product);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
    void updateProduct(UpdateProductByIdRequest updateProductByIdRequest, @MappingTarget Product product);
}

