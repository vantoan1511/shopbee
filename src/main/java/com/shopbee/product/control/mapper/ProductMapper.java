package com.shopbee.product.control.mapper;

import com.shopbee.product.entity.Product;
import com.shopbee.product.model.CreateProductRequest;
import com.shopbee.product.model.PatchProductByIdRequest;
import com.shopbee.product.model.ProductDTO;
import com.shopbee.product.model.UpdateProductByIdRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.net.URI;
import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

    ProductDTO toProductDTO(Product product);

    List<ProductDTO> toProducts(List<Product> products);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "tenantId", source = "tenantId")
    Product toProduct(String tenantId, CreateProductRequest createProductRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sku", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    void patchProduct(PatchProductByIdRequest patch, @MappingTarget Product product);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sku", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    void updateProduct(UpdateProductByIdRequest update, @MappingTarget Product product);

    default String map(URI value) {
        return (value == null) ? null : value.toString();
    }

    default URI map(String value) {
        return (value == null) ? null : URI.create(value);
    }
}

