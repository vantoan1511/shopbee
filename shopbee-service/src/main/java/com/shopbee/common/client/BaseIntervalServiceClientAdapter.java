package com.shopbee.common.client;

import com.shopbee.common.exception.TechnicalException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.Response;

import java.util.function.Supplier;

public abstract class BaseIntervalServiceClientAdapter {

    protected final <T> T handle(Supplier<Response> responseSupplier, Class<T> responseType) {
        try (Response response = responseSupplier.get();) {
            ensureSuccess(response);
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
            throw new TechnicalException("INVALID_REMOTE_RESPONSE", "Failed to parse remote response", e);
        }
    }

    private void ensureSuccess(Response response) {
        if (response.getStatus() >= 400) {
            throw new TechnicalException("REMOTE_SERVICE_ERROR", "Remote service failed: " + response.getStatus(), null);
        }
    }

    private RuntimeException mapProcessingException(ProcessingException e, String serviceName) {
        return new TechnicalException(serviceName + "_UNAVAILABLE", serviceName + " unreachable", e);
    }
}
