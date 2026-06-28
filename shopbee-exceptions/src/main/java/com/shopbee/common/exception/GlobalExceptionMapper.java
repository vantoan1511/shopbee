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
        if (throwable instanceof ApplicationException e) {
            return Response.status(e.getResponse().getStatus())
                    .entity(ExceptionResponse.of(e.getCode(), e.getMessage()))
                    .build();
        }

        LOG.error("Unexpected error ", throwable);

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ExceptionResponse.of("SYSTEM_ERROR", "An unexpected error occurred."))
                .build();
    }
}
