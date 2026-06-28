package com.shopbee.security.context;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

@Provider
@Priority(Priorities.USER + 100)
public class TenantContextCleanUpFilter implements ContainerResponseFilter {

    private final TenantContextHolder holder;

    public TenantContextCleanUpFilter(TenantContextHolder holder) {
        this.holder = holder;
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        holder.clear();
    }
}
