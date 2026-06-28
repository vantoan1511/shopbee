package com.shopbee.common.exception;

import jakarta.ws.rs.core.Response;

public class ForbiddenException extends BusinessException {
    public ForbiddenException(String code, String message) {
        super(Response.Status.FORBIDDEN.getStatusCode(), code, message);
    }
}
