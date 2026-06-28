package com.shopbee.common.exception;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;

import java.util.Optional;

@Provider
public class ServiceClientExceptionMapper implements ResponseExceptionMapper<WebApplicationException> {

    @Override
    public ApplicationException toThrowable(Response response) {
        ExceptionResponse errorResponse = readErrorResponse(response);
        String code = Optional.ofNullable(errorResponse).map(ExceptionResponse::code).orElse("REMOTE_SERVICE_ERROR");
        String message = Optional.ofNullable(errorResponse).map(ExceptionResponse::message).orElse(response.getStatusInfo().getReasonPhrase());
        int status = response.getStatus();
        if (response.getStatusInfo().getFamily() == Response.Status.Family.CLIENT_ERROR) {
            return new BusinessException(status, code, message);
        }
        return new TechnicalException(status, code, message);
    }

    private static ExceptionResponse readErrorResponse(Response response) {
        if (response.hasEntity()) {
            return response.readEntity(ExceptionResponse.class);
        }
        return null;
    }
}
