package com.shopbee.common.exception;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;

import java.util.Optional;

@Provider
public class ClientExceptionMapper implements ResponseExceptionMapper<WebApplicationException> {

    @Override
    public AppException toThrowable(Response response) {
        int status = response.getStatus();
        ErrorResponse errorResponse = readErrorResponse(response);
        String code = Optional.ofNullable(errorResponse).map(m -> m.code).orElse("REMOTE_SERVICE_ERROR");
        String message = Optional.ofNullable(errorResponse).map(m -> m.message).orElse(response.getStatusInfo().getReasonPhrase());
        if (response.getStatusInfo().getFamily() == Response.Status.Family.CLIENT_ERROR) {
            return new BusinessException(status, code, message);
        }
        return new TechnicalException(status, code, message);
    }

    @Override
    public boolean handles(int status, MultivaluedMap<String, Object> headers) {
        return status >= 400;
    }

    private static ErrorResponse readErrorResponse(Response response) {
        if (response.hasEntity()) {
            return response.readEntity(ErrorResponse.class);
        }
        return null;
    }
}
