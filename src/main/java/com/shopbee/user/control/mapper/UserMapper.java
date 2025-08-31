/*
 * UserMapper.java
 *
 * Copyright by shopbee-user-service, all rights reserved.
 * MIT License: https://mit-license.org
 */

package com.shopbee.user.control.mapper;

import com.shopbee.user.entity.User;
import com.shopbee.user.model.CreateUserRequest;
import com.shopbee.user.model.PatchUserByIdRequest;
import com.shopbee.user.model.UpdateUserByIdRequest;
import com.shopbee.user.model.UserDTO;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI, unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {AddressMapper.class, PhoneMapper.class})
public interface UserMapper {

    @Mapping(target = "addresses", ignore = true)
    List<UserDTO> toUsers(List<User> users);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "mainAddress", ignore = true)
    @Mapping(target = "phone", ignore = true)
    @Mapping(target = "tenantId", source = "tenantId")
    User toUser(String tenantId, CreateUserRequest createUserRequest);

    @Mapping(target = "mainAddress", ignore = true)
    UserDTO toUserDTO(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "phone", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "mainAddress", ignore = true)
    void patchUser(PatchUserByIdRequest patchUserByIdRequest, @MappingTarget User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "mainAddress", ignore = true)
    @Mapping(target = "phone", ignore = true)
    void updateUser(UpdateUserByIdRequest updateUserByIdRequest, @MappingTarget User user);
}
