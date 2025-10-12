/*
 * KeycloakTenantResolver.java
 *
 * Copyright by shopbee-service, all rights reserved.
 * MIT License: https://mit-license.org
 */

package com.shopbee.security;


import io.quarkus.oidc.OidcRequestContext;
import io.quarkus.oidc.OidcTenantConfig;
import io.quarkus.oidc.TenantConfigResolver;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.UriBuilder;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class KeycloakTenantResolver implements TenantConfigResolver {

    private static final Logger LOG = LoggerFactory.getLogger(KeycloakTenantResolver.class);
    private static final String REALMS_PATH = "/realms/";
    private final Map<String, String> tenantIdToAuthServerUrlCache = new ConcurrentHashMap<>();

    @ConfigProperty(name = "shopbee.security.keycloak.url")
    private String keycloakUrl;

    @Override
    public Uni<OidcTenantConfig> resolve(RoutingContext routingContext, OidcRequestContext<OidcTenantConfig> requestContext) {
        String tenantId = routingContext.request().getHeader("tenantId");
        if (StringUtils.isBlank(tenantId)) {
            LOG.error("Missing tenantId in request header");
            return Uni.createFrom().nullItem();
        }
        if (!tenantId.matches("^[a-zA-Z0-9_-]+$")) {
            LOG.error("Invalid tenantId format: {}", tenantId);
            return Uni.createFrom().nullItem();
        }

        String authServerUrl = tenantIdToAuthServerUrlCache.computeIfAbsent(tenantId, (key) -> {
            URI keycloakUri = URI.create(keycloakUrl);
            URI authServerUri = UriBuilder.fromUri(keycloakUri).path(REALMS_PATH + tenantId).build();
            return authServerUri.toString();
        });

        LOG.debug("Resolved authServerUrl: {} for tenantId: {}", authServerUrl, tenantId);

        return Uni.createFrom().item(OidcTenantConfig.builder().tenantId(tenantId).authServerUrl(authServerUrl).build());
    }
}
