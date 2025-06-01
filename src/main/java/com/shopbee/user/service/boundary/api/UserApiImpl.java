/*
 * UserApiImpl.java
 *
 * Copyright by shopbee-service, all rights reserved.
 * MIT License: https://mit-license.org
 */

package com.shopbee.user.service.boundary.api;

import com.shopbee.api.UsersApi;
import com.shopbee.model.CreateUserAddressRequest;
import com.shopbee.model.CreateUserRequest;
import com.shopbee.model.PatchUserAddressRequest;
import com.shopbee.model.PatchUserByIdRequest;
import com.shopbee.model.UpdateUserByIdRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class UserApiImpl implements UsersApi {

    @Override
    public Response createUser(String tenantId, CreateUserRequest createUserRequest) {
        return Response.status(Response.Status.NOT_IMPLEMENTED).build();
    }

    @Override
    public Response createUserAddress(String tenantId, String userId, CreateUserAddressRequest createUserAddressRequest) {
        return Response.status(Response.Status.NOT_IMPLEMENTED).build();
    }

    @Override
    public Response deleteUserAddress(String tenantId, String userId, String addressId) {
        return Response.status(Response.Status.NOT_IMPLEMENTED).build();
    }

    @Override
    public Response deleteUserById(String tenantId, String userId) {
        return Response.status(Response.Status.NOT_IMPLEMENTED).build();
    }

    @Override
    public Response getUserAddresses(String tenantId, String userId, Integer offset, Integer limit) {
        return Response.status(Response.Status.NOT_IMPLEMENTED).build();
    }

    @Override
    public Response getUserById(String tenantId, String userId) {
        return Response.status(Response.Status.NOT_IMPLEMENTED).build();
    }

    @Override
    public Response getUsers(String tenantId, Integer offset, Integer limit) {
        return Response.status(Response.Status.NOT_IMPLEMENTED).build();
    }

    @Override
    public Response patchUserAddress(String tenantId, String userId, String addressId, PatchUserAddressRequest patchUserAddressRequest) {
        return Response.status(Response.Status.NOT_IMPLEMENTED).build();
    }

    @Override
    public Response patchUserById(String tenantId, String userId, PatchUserByIdRequest patchUserByIdRequest) {
        return Response.status(Response.Status.NOT_IMPLEMENTED).build();
    }

    @Override
    public Response updateUserAddress(String tenantId, String userId, String addressId, CreateUserAddressRequest createUserAddressRequest) {
        return Response.status(Response.Status.NOT_IMPLEMENTED).build();
    }

    @Override
    public Response updateUserById(String tenantId, String userId, UpdateUserByIdRequest updateUserByIdRequest) {
        return Response.status(Response.Status.NOT_IMPLEMENTED).build();
    }
}
