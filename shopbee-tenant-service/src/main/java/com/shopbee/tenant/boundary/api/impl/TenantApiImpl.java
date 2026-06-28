/*
 * TenantApiImpl.java
 *
 * Copyright by shopbee-service, all rights reserved.
 * MIT License: https://mit-license.org
 */

package com.shopbee.tenant.boundary.api.impl;

import com.shopbee.security.Role;
import com.shopbee.tenant.boundary.api.TenantsApi;
import com.shopbee.tenant.control.service.TenantService;
import com.shopbee.tenant.dto.CreateTenantClientRequest;
import com.shopbee.tenant.dto.CreateTenantRequest;
import com.shopbee.tenant.dto.UpdateTenantByIdRequest;
import com.shopbee.tenant.dto.UpdateTenantClientByIdRequest;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;

@RolesAllowed(Role.PLATFORM_ADMINISTRATOR)
public class TenantApiImpl implements TenantsApi {

    private final TenantService tenantService;
    private final UriInfo uriInfo;

    public TenantApiImpl(TenantService tenantService, UriInfo uriInfo) {
        this.tenantService = tenantService;
        this.uriInfo = uriInfo;
    }

    @Override
    @PermitAll
    @Deprecated
    public Response bootstrapDefaultTenant(String adminSecret) {
        return Response.ok().build();
    }

    @Override
    public Response getAllTenants(String tenantId, Integer page, Integer size) {
        return Response.ok(tenantService.getAllTenants(tenantId, page, size)).build();
    }

    @Override
    public Response getTenantById(String tenantId, String tenantName) {
        return Response.ok(tenantService.getTenantByName(tenantId, tenantName)).build();
    }

    @Override
    public Response createTenant(String tenantId, CreateTenantRequest createTenantRequest) {
        String createdTenantName = tenantService.createTenant(tenantId, createTenantRequest);
        URI location = uriInfo.getAbsolutePathBuilder().path(createdTenantName).build();
        return Response.created(location).build();
    }

    @Override
    public Response updateTenantById(String tenantId, String tenantName, UpdateTenantByIdRequest updateTenantByIdRequest) {
        tenantService.updateTenantById(tenantId, tenantName, updateTenantByIdRequest);
        return Response.ok().build();
    }

    @Override
    public Response deleteTenantById(String tenantId, String tenantName) {
        tenantService.deleteTenantById(tenantId, tenantName);
        return Response.noContent().build();
    }

    @Override
    public Response getTenantClients(String tenantId, String tenantName, Integer offset, Integer limit) {
        return Response.ok(tenantService.getTenantClients(tenantId, tenantName, offset, limit)).build();
    }

    @Override
    public Response getTenantClientById(String tenantId, String tenantName, String clientId) {
        return Response.ok(tenantService.getTenantClientById(tenantId, tenantName, clientId)).build();
    }

    @Override
    public Response createTenantClient(String tenantId, String tenantName, CreateTenantClientRequest createTenantClientRequest) {
        String createdClientId = tenantService.createTenantClient(tenantId, tenantName, createTenantClientRequest);
        URI location = uriInfo.getAbsolutePathBuilder().path(createdClientId).build();
        return Response.created(location).build();
    }

    @Override
    public Response updateTenantClientById(String tenantId, String tenantName, String clientId, UpdateTenantClientByIdRequest updateTenantClientByIdRequest) {
        tenantService.updateTenantClientById(tenantId, tenantName, clientId, updateTenantClientByIdRequest);
        return Response.ok().build();
    }

    @Override
    public Response deleteTenantClient(String tenantId, String tenantName, String clientId) {
        tenantService.deleteTenantClient(tenantId, tenantName, clientId);
        return Response.noContent().build();
    }
}
