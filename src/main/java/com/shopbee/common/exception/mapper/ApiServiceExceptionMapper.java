/*
 * UserServiceExceptionMapper.java
 *
 * Copyright by shopbee-service, all rights reserved.
 * MIT License: https://mit-license.org
 */

package com.shopbee.common.exception.mapper;

import com.shopbee.common.exception.dto.ApiServiceException;
import com.shopbee.common.exception.dto.Error;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ApiServiceExceptionMapper implements ExceptionMapper<ApiServiceException> {

    @Override
    public Response toResponse(ApiServiceException e) {
        return Response.status(e.getResponse().getStatus()).entity(new Error(e.getMessage())).build();
    }
}
