package com.shopbee.security.auth;

import com.shopbee.initialization.service.KeycloakConfig;
import com.shopbee.security.TokenCache;
import com.shopbee.security.context.TenantContext;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.lang3.StringUtils;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.AccessTokenResponse;

@ApplicationScoped
public class DefaultTokenProvider implements TokenProvider {

    private static final String DEFAULT_KEYCLOAK_URL = "http://shopbee-keycloak:8080/";

    private final KeycloakConfig keycloakConfig;
    private final TokenCache tokenCache;
    private final TenantContext tenantContext;

    public DefaultTokenProvider(KeycloakConfig keycloakConfig, TokenCache tokenCache, TenantContext tenantContext) {
        this.keycloakConfig = keycloakConfig;
        this.tokenCache = tokenCache;
        this.tenantContext = tenantContext;
    }

    @Override
    public String getApplicationAdministrationToken() {
        return getApplicationAdministrationToken(tenantContext.getTenantId());
    }

    @Override
    public String getBusinessAdministrationToken() {
        return getBusinessAdministrationToken(tenantContext.getTenantId());
    }

    @Override
    public String getApplicationAdministrationToken(String tenantId) {
        return getServiceAccountToken(
                tenantId,
                RoleName.APPLICATION_ADMINISTRATION,
                keycloakConfig.clients().applicationAdministration().secret()
        );
    }

    @Override
    public String getBusinessAdministrationToken(String tenantId) {
        return getServiceAccountToken(
                tenantId,
                RoleName.BUSINESS_ADMINISTRATION,
                keycloakConfig.clients().businessAdministration().secret()
        );
    }

    private String getServiceAccountToken(String tenantId, String clientId, String clientSecret) {
        String tokenCacheKey = String.join("-", tenantId, clientId, "token");
        String refreshTokenCacheKey = String.join("-", tenantId, clientId, "refresh-token");

        String token = tokenCache.get(tokenCacheKey);

        if (StringUtils.isBlank(token)) {
            try (Keycloak keycloak = KeycloakBuilder.builder()
                    .serverUrl(keycloakConfig.url().orElse(DEFAULT_KEYCLOAK_URL))
                    .realm(tenantId)
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                    .build()) {

                AccessTokenResponse accessToken = keycloak.tokenManager().getAccessToken();

                String newToken = accessToken.getToken();
                String refreshToken = accessToken.getRefreshToken();

                tokenCache.put(tokenCacheKey, newToken, accessToken.getExpiresIn());
                tokenCache.put(refreshTokenCacheKey, refreshToken, accessToken.getRefreshExpiresIn());

                return newToken;
            }
        }

        return token;
    }
}
