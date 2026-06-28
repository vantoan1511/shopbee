/*
 * AddressMapper.java
 *
 * Copyright by shopbee-user-service, all rights reserved.
 * MIT License: https://mit-license.org
 */

package com.shopbee.user.control.mapper;

import com.shopbee.user.entity.Address;
import com.shopbee.user.model.AddressDTO;
import com.shopbee.user.model.CreateUserAddressRequest;
import com.shopbee.user.model.PatchUserAddressRequest;
import org.apache.commons.lang3.EnumUtils;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AddressMapper {

    List<AddressDTO> toAddressesDto(List<Address> source);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "type", qualifiedByName = "mapAddressTypeToStringValue")
    @Mapping(target = "tenantId", source = "tenantId")
    Address toAddress(String tenantId, CreateUserAddressRequest source);

    AddressDTO toAddress(Address source);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "type", qualifiedByName = "mapAddressTypeToStringValue")
    void updateAddress(CreateUserAddressRequest source, @MappingTarget Address target);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "type", qualifiedByName = "mapAddressTypeToStringValue")
    void patchAddress(PatchUserAddressRequest source, @MappingTarget Address target);

    @Named("mapAddressTypeToStringValue")
    default Address.Type mapAddressTypeFromStringValue(String value) {
        return EnumUtils.getEnum(Address.Type.class, value);
    }
}