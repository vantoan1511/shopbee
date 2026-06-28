package com.shopbee.common.exception;

import io.quarkus.oidc.OIDCException;
import io.vertx.ext.web.RoutingContext;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class OIDCExceptionMapper implements ExceptionMapper<OIDCException> {

    @Inject
    RoutingContext context;

    @Override
    public Response toResponse(OIDCException exception) {
        String tenantId = context.request().getHeader("tenantId");
        return Response.status(Response.Status.UNAUTHORIZED)
                .entity(ErrorResponse.of("OIDC_ERROR", exception.getMessage() + " at " + tenantId))
                .build();
    }
}
