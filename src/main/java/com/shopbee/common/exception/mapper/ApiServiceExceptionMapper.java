/*
 * UserServiceExceptionMapper.java
 *
 * Copyright by shopbee-service, all rights reserved.
 * MIT License: https://mit-license.org
 */

package com.shopbee.common.exception.mapper;

import com.shopbee.common.exception.ApiServiceException;
import com.shopbee.common.exception.dto.Error;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class ApiServiceExceptionMapper implements ExceptionMapper<ApiServiceException> {
    private static final Logger LOG = LoggerFactory.getLogger(ApiServiceExceptionMapper.class);

    @Override
    public Response toResponse(ApiServiceException e) {
        LOG.warn("API error: {} ({})", e.getMessage(), e.getResponse().getStatus());
        return Response.status(e.getResponse().getStatus()).entity(new Error(e.getMessage())).build();
    }
}
