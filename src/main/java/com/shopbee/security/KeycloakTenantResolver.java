/*
 * KeycloakTenantResolver.java
 *
 * Copyright by shopbee-service, all rights reserved.
 * MIT License: https://mit-license.org
 */

package com.shopbee.security;


import com.shopbee.tenant.control.repository.TenantRepository;
import com.shopbee.tenant.entity.Tenant;
import com.shopbee.tenant.entity.type.Status;
import io.quarkus.oidc.OidcRequestContext;
import io.quarkus.oidc.OidcTenantConfig;
import io.quarkus.oidc.TenantConfigResolver;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
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
    private final TenantRepository tenantRepository;

    public KeycloakTenantResolver(SecurityConfig securityConfig, TenantRepository tenantRepository) {
        this.securityConfig = securityConfig;
        this.tenantRepository = tenantRepository;
    }

    @Override
    @ActivateRequestContext
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
                .emitOn(Infrastructure.getDefaultWorkerPool())
                .onItem().transform(tenantRepository::findByName)
                .onItem().transform(this::buildOidcTenantConfig);
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

    private OidcTenantConfig buildOidcTenantConfig(Tenant tenant) {
        if (tenant == null) {
            LOG.warn("Tenant not found");
            return null;
        }
        if (tenant.getStatus() == Status.INACTIVE) {
            LOG.warn("Tenant {} is inactive", tenant.getName());
            return null;
        }
        String tenantId = tenant.getName();

        return tenantConfigCache.computeIfAbsent(tenantId, key -> {
            String authServerUrl = buildAuthServerUrl(tenantId);
            String issuer = buildIssuer(tenantId);

            LOG.debug("Resolve tenant config [tenant={}, authServerUrl={}, issuer={}]", tenantId, authServerUrl, issuer);

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
