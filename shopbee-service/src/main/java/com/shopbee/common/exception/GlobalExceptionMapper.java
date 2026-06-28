package com.shopbee.common.exception;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
@Priority(Priorities.USER)
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {
    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionMapper.class);

    @Override
    public Response toResponse(Throwable throwable) {
        if (throwable instanceof AppException appException) {
            return Response.status(appException.getResponse().getStatus())
                    .entity(ErrorResponse.of(appException.getCode(), appException.getMessage()))
                    .build();
        }

        LOG.error("Unexpected error: {}", throwable.getMessage(), throwable);

        return Response.status(500).entity(ErrorResponse.of("SYSTEM_ERROR", "An unexpected error occurred.")).build();
    }
}
