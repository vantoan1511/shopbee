package com.shopbee.security.context;

import com.shopbee.security.auth.DownstreamTokenResolver;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;

import java.io.IOException;

public class InternalSecurityHeadersFilter implements ClientRequestFilter {

    private final DownstreamTokenResolver tokenResolver;
    private final TenantContext tenantContext;

    public InternalSecurityHeadersFilter(DownstreamTokenResolver tokenResolver, TenantContext tenantContext) {
        this.tokenResolver = tokenResolver;
        this.tenantContext = tenantContext;
    }

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        requestContext.getHeaders().putSingle("tenantId", tenantContext.getTenantId());
        requestContext.getHeaders().putSingle("Authorization", "Bearer " + tokenResolver.resolve());
    }
}
