package com.shopbee.common.exception;

import jakarta.ws.rs.core.Response;

public class ResourceConflictException extends BusinessException {
    public ResourceConflictException(String code, String message) {
        super(Response.Status.CONFLICT.getStatusCode(), code, message);
    }
}
