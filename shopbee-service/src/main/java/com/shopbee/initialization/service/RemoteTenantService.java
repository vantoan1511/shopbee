package com.shopbee.initialization.service;

import com.shopbee.tenant.boundary.api.TenantsApi;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "shopbee-tenant-service")
@RegisterClientHeaders
@ApplicationScoped
public interface RemoteTenantService extends TenantsApi {
}
