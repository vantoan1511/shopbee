package com.shopbee.common.client;

import com.shopbee.security.auth.TokenProvider;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;

import java.util.Objects;

@Provider
public class DefaultInternalClientHeadersFactory implements ClientHeadersFactory {

    private static final String TENANT_ID = "tenantId";
    private final TokenProvider tokenProvider;

    public DefaultInternalClientHeadersFactory(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public MultivaluedMap<String, String> update(MultivaluedMap<String, String> incomingHeaders, MultivaluedMap<String, String> clientOutgoingHeaders) {
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        String tenantId = incomingHeaders.getFirst(TENANT_ID);
        String callerToken = incomingHeaders.getFirst(HttpHeaders.AUTHORIZATION);
        headers.putSingle(TENANT_ID, tenantId);
        headers.putSingle(HttpHeaders.AUTHORIZATION, Objects.requireNonNullElseGet(callerToken, () -> getAccessToken(tenantId)));
        return headers;
    }

    private String getAccessToken(String tenantId) {
        return "Bearer " + tokenProvider.getBusinessAdministrationToken(tenantId);
    }
}
