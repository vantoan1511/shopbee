package com.shopbee.common.exception;

public class InvalidDataException extends BusinessException {
    public InvalidDataException(String code, String message) {
        super(code, message);
    }
}
