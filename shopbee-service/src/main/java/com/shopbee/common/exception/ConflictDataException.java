package com.shopbee.common.exception;

import jakarta.ws.rs.core.Response;

public class ConflictDataException extends BusinessException {
    public ConflictDataException(String code, String message) {
        super(Response.Status.CONFLICT.getStatusCode(), code, message);
    }
}
