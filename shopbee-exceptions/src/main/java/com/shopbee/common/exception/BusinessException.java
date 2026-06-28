package com.shopbee.common.exception;

import jakarta.ws.rs.core.Response;

public class BusinessException extends ApplicationException {
    public BusinessException(String code, String message) {
        super(Response.Status.BAD_REQUEST.getStatusCode(), code, message);
    }

    protected BusinessException(int status, String code, String message) {
        super(status, code, message);
    }
}
