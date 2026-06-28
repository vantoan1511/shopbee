package com.shopbee.common.exception;

import jakarta.ws.rs.core.Response;

public class TechnicalException extends AppException {
    public TechnicalException(int status, String code, String message) {
        super(status, code, message);
    }

    public TechnicalException(String code, String message) {
        super(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), code, message);
    }

    public TechnicalException(String code, String message, Throwable cause) {
        super(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), code, message);
        initCause(cause);
    }
}
