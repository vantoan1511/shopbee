package com.shopbee.common.exception;

import jakarta.ws.rs.core.Response;

public class TechnicalException extends ApplicationException {
    public TechnicalException(String code, String message) {
        super(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), code, message);
    }

    public TechnicalException(String code, String message, Throwable cause) {
        super(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), code, message);
        initCause(cause);
    }

    protected TechnicalException(int status, String code, String message) {
        super(status, code, message);
    }
}
