/*
 * UserApiImpl.java
 *
 * Copyright by shopbee-service, all rights reserved.
 * MIT License: https://mit-license.org
 */

package com.shopbee.user.boundary.api;

import com.shopbee.user.control.service.UserService;
import com.shopbee.user.model.CreateUserAddressRequest;
import com.shopbee.user.model.CreateUserRequest;
import com.shopbee.user.model.PatchUserAddressRequest;
import com.shopbee.user.model.PatchUserByIdRequest;
import com.shopbee.user.model.UpdateUserByIdRequest;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;

public class UserApiImpl implements UsersApi {

    private final UserService userService;
    private final UriInfo uriInfo;

    @Inject
    public UserApiImpl(UserService userService, UriInfo uriInfo) {
        this.userService = userService;
        this.uriInfo = uriInfo;
    }

    @Override
    public Response createUser(String tenantId, CreateUserRequest createUserRequest) {
        String userId = userService.createUser(tenantId, createUserRequest);
        URI location = uriInfo.getAbsolutePathBuilder().path(userId).build();
        return Response.created(location).entity(userId).build();
    }

    @Override
    public Response createUserAddress(String tenantId, String userId, CreateUserAddressRequest createUserAddressRequest) {
        String addressId = userService.createUserAddress(tenantId, userId, createUserAddressRequest);
        URI location = uriInfo.getAbsolutePathBuilder().path(addressId).build();
        return Response.created(location).entity(addressId).build();
    }

    @Override
    public Response deleteUserAddress(String tenantId, String userId, String addressId) {
        userService.deleteUserAddress(tenantId, userId, addressId);
        return Response.noContent().build();
    }

    @Override
    public Response deleteUserById(String tenantId, String userId) {
        userService.deleteUserById(tenantId, userId);
        return Response.noContent().build();
    }

    @Override
    public Response getUserAddresses(String tenantId, String userId, Integer offset, Integer limit) {
        return Response.ok(userService.getUserAddresses(tenantId, userId, offset, limit)).build();
    }

    @Override
    public Response getUserById(String tenantId, String userId) {
        return Response.ok(userService.getUserById(tenantId, userId)).build();
    }

    @Override
    public Response getUsers(String tenantId, Integer offset, Integer limit) {
        return Response.ok(userService.getUsers(tenantId, offset, limit)).build();
    }

    @Override
    public Response patchUserAddress(String tenantId, String userId, String addressId, PatchUserAddressRequest patchUserAddressRequest) {
        userService.patchUserAddress(tenantId, userId, addressId, patchUserAddressRequest);
        return Response.ok().build();
    }

    @Override
    public Response patchUserById(String tenantId, String userId, PatchUserByIdRequest patchUserByIdRequest) {
        userService.patchUserById(tenantId, userId, patchUserByIdRequest);
        return Response.ok().build();
    }

    @Override
    public Response updateUserAddress(String tenantId, String userId, String addressId, CreateUserAddressRequest createUserAddressRequest) {
        userService.updateUserAddress(tenantId, userId, addressId, createUserAddressRequest);
        return Response.ok().build();
    }

    @Override
    public Response updateUserById(String tenantId, String userId, UpdateUserByIdRequest updateUserByIdRequest) {
        userService.updateUserById(tenantId, userId, updateUserByIdRequest);
        return Response.ok().build();
    }
}
