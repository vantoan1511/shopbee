/*
 * AddressMapper.java
 *
 * Copyright by shopbee-user-service, all rights reserved.
 * MIT License: https://mit-license.org
 */

package com.shopbee.user.control.mapper;

import com.shopbee.user.entity.Address;
import com.shopbee.user.model.CreateUserAddressRequest;
import com.shopbee.user.model.PatchUserAddressRequest;
import java.util.List;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper interface for converting between Address entities and DTOs.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.CDI, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AddressMapper {

    /**
     * Converts a list of Address entities to a list of Address DTOs.
     *
     * @param source the list of Address entities
     * @return the list of Address DTOs
     */
    List<com.shopbee.user.model.Address> toAddresses(List<Address> source);

    /**
     * Maps a com.shopbee.user.model.Address object to an Address entity.
     * Ignores the user field and maps the isDefault field to asDefault.
     *
     * @param source the source Address DTO
     * @return the mapped Address entity
     */
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "type", qualifiedByName = "mapAddressTypeToStringValue")
    Address toAddress(com.shopbee.user.model.Address source);

    /**
     * Maps a CreateUserAddressRequest object to an Address entity.
     * Ignores the id and user fields and maps the isDefault field to asDefault.
     *
     * @param source the source CreateUserAddressRequest DTO
     * @return the mapped Address entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "type", qualifiedByName = "mapAddressTypeToStringValue")
    @Mapping(target = "tenantId", source = "tenantId")
    Address toAddress(String tenantId, CreateUserAddressRequest source);

    /**
     * Maps an Address entity to a com.shopbee.user.model.Address object.
     * Maps the asDefault field to isDefault.
     *
     * @param source the source Address entity
     * @return the mapped Address DTO
     */
    com.shopbee.user.model.Address toAddress(Address source);

    /**
     * Updates an Address entity with values from a CreateUserAddressRequest DTO.
     *
     * @param source the source CreateUserAddressRequest DTO
     * @param target the target Address entity
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "type", qualifiedByName = "mapAddressTypeToStringValue")
    void updateAddress(CreateUserAddressRequest source, @MappingTarget Address target);

    /**
     * Partially updates an Address entity with values from a PatchUserAddressRequest DTO.
     *
     * @param source the source PatchUserAddressRequest DTO
     * @param target the target Address entity
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "type", qualifiedByName = "mapAddressTypeToStringValue")
    void patchAddress(PatchUserAddressRequest source, @MappingTarget Address target);

    /**
     * Maps a string value to an Address.Type enum.
     *
     * @param value the string value
     * @return the corresponding Address.Type enum
     */
    @Named("mapAddressTypeToStringValue")
    default Address.Type mapAddressTypeFromStringValue(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        return EnumUtils.getEnum(Address.Type.class, value.toUpperCase());
    }
}