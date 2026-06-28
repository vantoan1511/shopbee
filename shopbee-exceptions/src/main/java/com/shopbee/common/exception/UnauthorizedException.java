package com.shopbee.common.exception;

import jakarta.ws.rs.core.Response;

public class UnauthorizedException extends BusinessException {
    public UnauthorizedException(String code, String message) {
        super(Response.Status.UNAUTHORIZED.getStatusCode(), code, message);
    }
}
