/*
 * KeycloakTenantResolver.java
 *
 * Copyright by shopbee-service, all rights reserved.
 * MIT License: https://mit-license.org
 */

package com.shopbee.security;


import com.shopbee.security.config.SecurityConfig;
import io.quarkus.oidc.OidcRequestContext;
import io.quarkus.oidc.OidcTenantConfig;
import io.quarkus.oidc.TenantConfigResolver;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.UriBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class KeycloakTenantResolver implements TenantConfigResolver {
    private static final Logger LOG = LoggerFactory.getLogger(KeycloakTenantResolver.class);
    private static final String REALMS_PATH = "realms";
    private final Map<String, OidcTenantConfig> tenantConfigCache = new ConcurrentHashMap<>();

    private final SecurityConfig securityConfig;

    public KeycloakTenantResolver(SecurityConfig securityConfig) {
        this.securityConfig = securityConfig;
    }

    @Override
    public Uni<OidcTenantConfig> resolve(RoutingContext routingContext, OidcRequestContext<OidcTenantConfig> requestContext) {
        String tenantId = routingContext.request().getHeader("tenantId");
        if (StringUtils.isBlank(tenantId)) {
            LOG.warn("Missing tenantId in request header");
            return Uni.createFrom().nullItem();
        }
        if (!tenantId.matches("^[a-zA-Z0-9_-]+$")) {
            LOG.warn("Invalid tenantId format: {}", tenantId);
            return Uni.createFrom().nullItem();
        }
        return Uni.createFrom().item(tenantId)
                .onItem().transform(this::buildOidcTenantConfig)
                .onItem().invoke(oidcTenantConfig -> LOG.debug("Resolved tenant config for tenantId={}, clientId={}, authServerUrl={}, issuer={}", tenantId, oidcTenantConfig.clientId().orElse(""), oidcTenantConfig.authServerUrl().orElse(""), oidcTenantConfig.token().issuer().orElse("")));
    }

    /**
     * FOR TESTING PURPOSES ONLY.
     * Provides test classes in the same package access to the internal cache.
     *
     * @return The internal cache map.
     */
    Map<String, OidcTenantConfig> getCacheForTesting() {
        return tenantConfigCache;
    }

    private OidcTenantConfig buildOidcTenantConfig(String tenantId) {
        return tenantConfigCache.computeIfAbsent(tenantId, key -> {
            String authServerUrl = buildAuthServerUrl(tenantId);
            String issuer = buildIssuer(tenantId);

            return OidcTenantConfig.builder()
                    .tenantId(tenantId)
                    .authServerUrl(authServerUrl)
                    .clientId(securityConfig.clientId())
                    .token().issuer(issuer).end()
                    .build();
        });
    }

    private String buildAuthServerUrl(String tenantId) {
        return buildUri(tenantId, securityConfig.authServerUrl());
    }

    private String buildIssuer(String tenantId) {
        return buildUri(tenantId, securityConfig.issuer());
    }

    private String buildUri(String tenantId, String baseUrl) {
        URI baseUri = URI.create(baseUrl);
        URI resolvedUri = UriBuilder.fromUri(baseUri).replacePath("").segment(REALMS_PATH, tenantId).build();
        return resolvedUri.toString();
    }
}
