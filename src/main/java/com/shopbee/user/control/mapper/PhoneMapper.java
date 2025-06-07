/*
 * PhoneMapper.java
 *
 * Copyright by shopbee-user-service, all rights reserved.
 * MIT License: https://mit-license.org
 */

package com.shopbee.user.control.mapper;

import com.shopbee.user.entity.Phone;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PhoneMapper {

    /**
     * To phone.
     *
     * @param phone the phone
     * @return the phone
     */
    @Mapping(target = "id.countryCode", source = "countryCode")
    @Mapping(target = "id.number", source = "number")
    Phone toPhone(com.shopbee.user.model.Phone phone);

}
