/*
 * UserServiceExceptionMapper.java
 *
 * Copyright by shopbee-service, all rights reserved.
 * MIT License: https://mit-license.org
 */

package com.shopbee.user.control.exception;

import com.shopbee.common.exception.dto.Error;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class UserServiceExceptionMapper implements ExceptionMapper<UserServiceException> {

    @Override
    public Response toResponse(UserServiceException e) {
        return Response.status(e.getResponse().getStatus()).entity(new Error(e.getMessage())).build();
    }
}
