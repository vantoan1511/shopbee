/*
 * PhoneMapper.java
 *
 * Copyright by shopbee-user-service, all rights reserved.
 * MIT License: https://mit-license.org
 */

package com.shopbee.user.control.mapper;

import com.shopbee.user.entity.PhoneId;
import com.shopbee.user.model.Phone;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * The interface Phone mapper.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.CDI, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PhoneMapper {

    /**
     * To phone.
     *
     * @param phone the phone
     * @return the phone
     */
    PhoneId toPhone(Phone phone);
}
