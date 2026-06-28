package com.shopbee.security;

import io.quarkus.oidc.OidcRequestContext;
import io.quarkus.oidc.OidcTenantConfig;
import io.quarkus.oidc.TenantConfigResolver;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.UriBuilder;
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
    private final ShopbeeSecurityConfig shopbeeSecurityConfig;

    public KeycloakTenantResolver(ShopbeeSecurityConfig shopbeeSecurityConfig) {
        this.shopbeeSecurityConfig = shopbeeSecurityConfig;
    }

    @Override
    public Uni<OidcTenantConfig> resolve(RoutingContext routingContext, OidcRequestContext<OidcTenantConfig> requestContext) {
        String tenantId = routingContext.request().getHeader("tenantId");
        if (tenantId == null || tenantId.isEmpty()) {
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
                    .clientId(shopbeeSecurityConfig.clientId())
                    .token().issuer(issuer).end()
                    .build();
        });
    }

    private String buildAuthServerUrl(String tenantId) {
        return buildUri(tenantId, shopbeeSecurityConfig.authServerUrl());
    }

    private String buildIssuer(String tenantId) {
        return buildUri(tenantId, shopbeeSecurityConfig.issuer());
    }

    private String buildUri(String tenantId, String baseUrl) {
        URI baseUri = URI.create(baseUrl);
        URI resolvedUri = UriBuilder.fromUri(baseUri).replacePath("").segment(REALMS_PATH, tenantId).build();
        return resolvedUri.toString();
    }
}
