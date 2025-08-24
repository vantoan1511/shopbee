/*
 * UserServiceException.java
 *
 * Copyright by shopbee-service, all rights reserved.
 * MIT License: https://mit-license.org
 */

package com.shopbee.common.exception.dto;

import com.shopbee.common.StringFormatter;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

public class ApiServiceException extends WebApplicationException {

    private ApiServiceException(String message) {
        super(message);
    }

    private ApiServiceException(String message, Response.Status status) {
        super(message, status);
    }

    public static ApiServiceException badRequest(String message, Object... parameters) {
        return create(Response.Status.BAD_REQUEST, message, parameters);
    }

    public static ApiServiceException notFound(String message, Object... parameters) {
        return create(Response.Status.NOT_FOUND, message, parameters);
    }

    public static ApiServiceException conflict(String message, Object... parameters) {
        return create(Response.Status.CONFLICT, message, parameters);
    }

    public static ApiServiceException internalServerError(String message, Object... parameters) {
        return create(Response.Status.INTERNAL_SERVER_ERROR, message, parameters);
    }

    public static ApiServiceException unavailable(String message, Object... parameters) {
        return create(Response.Status.SERVICE_UNAVAILABLE, message, parameters);
    }

    public static ApiServiceException create(Response.Status status, String message, Object... parameters) {
        return new ApiServiceException(StringFormatter.format(message, parameters), status);
    }
}
