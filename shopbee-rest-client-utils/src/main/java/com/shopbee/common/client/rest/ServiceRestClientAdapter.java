package com.shopbee.common.client.rest;

import com.shopbee.common.exception.TechnicalException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.Response;

import java.util.function.Supplier;

public abstract class ServiceRestClientAdapter {

    protected final <T> T handle(Supplier<Response> responseSupplier, Class<T> responseType) {
        try (Response response = responseSupplier.get();) {
            return readEntity(response, responseType);
        } catch (ProcessingException e) {
            throw mapProcessingException(e, getServiceName());
        }
    }

    protected abstract String getServiceName();

    private <T> T readEntity(Response response, Class<T> responseType) {
        try {
            return response.readEntity(responseType);
        } catch (Exception e) {
            throw new TechnicalException("INVALID_RESPONSE", "Failed to parse remote response", e);
        }
    }

    private RuntimeException mapProcessingException(ProcessingException e, String serviceName) {
        return new TechnicalException("SERVICE_UNAVAILABLE", serviceName + " unreachable", e);
    }
}
