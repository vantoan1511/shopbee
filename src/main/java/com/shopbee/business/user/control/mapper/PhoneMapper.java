/*
 * PhoneMapper.java
 *
 * Copyright by shopbee-user-service, all rights reserved.
 * MIT License: https://mit-license.org
 */

package com.shopbee.business.user.control.mapper;

import com.shopbee.business.user.entity.Phone;
import com.shopbee.user.model.PhoneDTO;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PhoneMapper {

    @Mapping(target = "countryCode", source = "id.countryCode")
    @Mapping(target = "number", source = "id.number")
    PhoneDTO toPhoneDTO(Phone phone);

    @Mapping(target = "id", source = "phoneDTO")
    @Mapping(target = "tenantId", expression = "java(tenantId)")
    Phone toPhone(PhoneDTO phoneDTO, @Context String tenantId);
}
