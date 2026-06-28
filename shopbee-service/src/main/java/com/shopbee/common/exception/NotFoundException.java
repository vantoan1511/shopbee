package com.shopbee.common.exception;

public class NotFoundException extends BusinessException {
    public NotFoundException(String code, String message) {
        super(404, code, message);
    }
}
