/*
 * Error.java
 *
 * Copyright by shopbee-service, all rights reserved.
 * MIT License: https://mit-license.org
 */

package com.shopbee.common.exception;

public class ErrorResponse {
    public String code;
    public String message;

    public static ErrorResponse of(String code, String message) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.code = code;
        errorResponse.message = message;
        return errorResponse;
    }
}
