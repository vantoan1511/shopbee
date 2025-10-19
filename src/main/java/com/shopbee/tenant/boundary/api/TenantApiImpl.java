package com.shopbee.tenant.boundary.api;

import com.shopbee.tenant.model.CreateTenantRequest;
import jakarta.ws.rs.core.Response;

public class TenantApiImpl implements TenantsApi {

    @Override
    public Response createTenant(CreateTenantRequest createTenantRequest) {
        return Response.status(Response.Status.NOT_IMPLEMENTED).build();
    }

    @Override
    public Response getTenants() {
        return Response.status(Response.Status.NOT_IMPLEMENTED).build();
    }

    @Override
    public Response getTenantById(String tenantId) {
        return Response.status(Response.Status.NOT_IMPLEMENTED).build();
    }

    @Override
    public Response updateTenantById(String tenantId, CreateTenantRequest createTenantRequest) {
        return Response.status(Response.Status.NOT_IMPLEMENTED).build();
    }

    @Override
    public Response deleteTenantById(String tenantId) {
        return Response.status(Response.Status.NOT_IMPLEMENTED).build();
    }
}
