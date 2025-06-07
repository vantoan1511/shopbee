/*
 * TenantApiImpl.java
 *
 * Copyright by shopbee-service, all rights reserved.
 * MIT License: https://mit-license.org
 */

package com.shopbee.tenant.boundary.api;

import com.shopbee.tenant.model.CreateTenantRequest;
import jakarta.ws.rs.core.Response;

public class TenantApiImpl implements TenantsApi {

    @Override
    public Response createTenant(CreateTenantRequest createTenantRequest) {
        return Response.status(Response.Status.NOT_IMPLEMENTED).build();
    }

    @Override
    public Response getTenantById(Long tenantId) {
        return Response.status(Response.Status.NOT_IMPLEMENTED).build();
    }

    @Override
    public Response getTenants() {
        return Response.status(Response.Status.NOT_IMPLEMENTED).build();
    }
}
