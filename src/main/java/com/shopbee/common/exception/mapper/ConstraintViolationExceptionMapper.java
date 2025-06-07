/*
 * ConstraintViolationExceptionMapper.java
 *
 * Copyright by shopbee-service, all rights reserved.
 * MIT License: https://mit-license.org
 */

package com.shopbee.common.exception.mapper;

import com.shopbee.common.exception.dto.ViolationError;
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

        Set<ViolationError> violations = e.getConstraintViolations().stream()
                .map(ConstraintViolationExceptionMapper::from)
                .collect(Collectors.toSet());

        return Response.status(Response.Status.BAD_REQUEST).entity(violations).build();
    }

    private static ViolationError from(ConstraintViolation<?> constraintViolation) {
        if (Objects.isNull(constraintViolation)) {
            return null;
        }

        ViolationError violationError = new ViolationError();
        violationError.setField(constraintViolation.getPropertyPath().toString());
        violationError.setValue(constraintViolation.getInvalidValue() != null ? constraintViolation.getInvalidValue().toString() : null);
        violationError.setMessage(constraintViolation.getMessage());
        return violationError;
    }
}
