package com.shopbee.tenant.control.exception;

import com.shopbee.common.exception.ExceptionResponse;
import io.quarkus.oidc.OIDCException;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class OIDCExceptionMapper implements ExceptionMapper<OIDCException> {

    @Override
    public Response toResponse(OIDCException exception) {
        ExceptionResponse oidcNotAvailable = ExceptionResponse.of("OIDC_NOT_AVAILABLE", "OpenID Connect provider is currently unavailable. Please try again later.");
        return Response.status(503).entity(oidcNotAvailable).build();
    }
}
