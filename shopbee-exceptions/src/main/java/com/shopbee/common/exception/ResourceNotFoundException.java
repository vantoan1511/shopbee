package com.shopbee.common.exception;

import jakarta.ws.rs.core.Response;

public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String code, String message) {
        super(Response.Status.NOT_FOUND.getStatusCode(), code, message);
    }
}
