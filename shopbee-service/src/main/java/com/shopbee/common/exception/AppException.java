package com.shopbee.common.exception;

import jakarta.ws.rs.WebApplicationException;

public abstract class AppException extends WebApplicationException {

    private final String code;

    protected AppException(int status, String code, String message) {
        super(message, status);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
