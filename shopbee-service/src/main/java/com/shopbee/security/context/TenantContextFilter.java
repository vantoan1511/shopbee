package com.shopbee.security.context;

import jakarta.annotation.Priority;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class TenantContextFilter implements ContainerRequestFilter {

    private final TenantContextHolder holder;

    public TenantContextFilter(TenantContextHolder holder) {
        this.holder = holder;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String tenantId = requestContext.getHeaderString("tenantId");

        if (tenantId == null || tenantId.isBlank()) {
            throw new BadRequestException("Missing or invalid tenantId header");
        }

        holder.set(tenantId);
    }
}
