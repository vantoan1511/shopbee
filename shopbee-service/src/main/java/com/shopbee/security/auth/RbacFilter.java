package com.shopbee.security.auth;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;

@Provider
@Priority(Priorities.AUTHORIZATION)
public class RbacFilter implements ContainerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(RbacFilter.class);
    private final ResourceInfo resourceInfo;
    private final TokenPermissionExtractor extractor;

    public RbacFilter(ResourceInfo resourceInfo, TokenPermissionExtractor extractor) {
        this.resourceInfo = resourceInfo;
        this.extractor = extractor;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        RequiresPermissions annotation = resourceInfo.getResourceMethod().getAnnotation(RequiresPermissions.class);

        if (annotation == null) {
            annotation = resourceInfo.getResourceClass().getAnnotation(RequiresPermissions.class);
        }

        if (annotation == null) {
            return;
        }

        Set<Permission> granted = extractor.getPermissions();

        for (Permission required : annotation.value()) {
            if (!granted.contains(required)) {
                LOG.warn("Access denied. Required: {}, granted {}, path {}", required, granted, requestContext.getUriInfo().getPath());
                requestContext.abortWith(Response.status(403).build());
                return;
            }
        }
    }
}
