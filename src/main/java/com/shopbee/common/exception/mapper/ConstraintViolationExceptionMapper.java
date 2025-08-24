/*
 * ConstraintViolationExceptionMapper.java
 *
 * Copyright by shopbee-service, all rights reserved.
 * MIT License: https://mit-license.org
 */

package com.shopbee.common.exception.mapper;

import com.shopbee.common.exception.dto.ValidationError;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException e) {

        Set<ValidationError> violations = e.getConstraintViolations().stream()
                .map(ConstraintViolationExceptionMapper::from)
                .collect(Collectors.toSet());

        return Response.status(Response.Status.BAD_REQUEST).entity(violations).build();
    }

    private static ValidationError from(ConstraintViolation<?> constraintViolation) {
        if (Objects.isNull(constraintViolation)) {
            return null;
        }

        ValidationError validationError = new ValidationError();
        validationError.setProperty(constraintViolation.getPropertyPath().toString());
        validationError.setValue(getValue(constraintViolation));
        validationError.setMessage(constraintViolation.getMessage());

        return validationError;
    }

    private static String getValue(ConstraintViolation<?> constraintViolation) {
        Object invalidValue = constraintViolation.getInvalidValue();
        if (invalidValue == null) {
            return "";
        }
        return invalidValue.toString();
    }
}
