/*
 * UserServiceException.java
 *
 * Copyright by shopbee-service, all rights reserved.
 * MIT License: https://mit-license.org
 */

package com.shopbee.user.control.exception;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

public class UserServiceException extends WebApplicationException {

    private UserServiceException(String message) {
        super(message);
    }

    private UserServiceException(String message, Response.Status status) {
        super(message, status);
    }

    public static UserServiceException badRequest(String message) {
        return new UserServiceException(message, Response.Status.BAD_REQUEST);
    }

    public static UserServiceException notFound(String message) {
        return new UserServiceException(message, Response.Status.NOT_FOUND);
    }

    public static UserServiceException conflict(String message) {
        return new UserServiceException(message, Response.Status.CONFLICT);
    }

    public static UserServiceException unavailable(String message) {
        return new UserServiceException(message, Response.Status.SERVICE_UNAVAILABLE);
    }
}
