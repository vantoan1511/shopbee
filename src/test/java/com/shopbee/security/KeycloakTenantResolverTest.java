package com.shopbee.security;

import com.shopbee.business.tenant.control.repository.TenantRepository;
import com.shopbee.business.tenant.entity.Tenant;
import io.quarkus.oidc.OidcTenantConfig;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class KeycloakTenantResolverTest {

    private final TenantRepository tenantRepository = mock();
    private final SecurityConfig securityConfig = mock();
    private final RoutingContext routingContext = mock();
    private final HttpServerRequest httpServerRequest = mock();
    private final Tenant tenant = mock();
    private final KeycloakTenantResolver keycloakTenantResolver = new KeycloakTenantResolver(securityConfig, tenantRepository);

    @BeforeEach
    void setUp() {
        keycloakTenantResolver.getCacheForTesting().clear();

        when(routingContext.request()).thenReturn(httpServerRequest);
    }

    @Test
    @DisplayName("Should resolve tenant URL when given a valid tenantId")
    void testResolve_withValidTenant_shouldResolveUrl() {
        String tenantId = "tenant-a";
        when(httpServerRequest.getHeader("tenantId")).thenReturn(tenantId);
        when(tenantRepository.findByName(tenantId)).thenReturn(tenant);
        when(tenant.getName()).thenReturn(tenantId);
        when(tenant.getStatus()).thenReturn(Tenant.Status.ACTIVE);
        when(securityConfig.authServerUrl()).thenReturn("http://localhost:8080/realms/master");
        when(securityConfig.issuer()).thenReturn("http://localhost:8080/realms/master");
        when(securityConfig.clientId()).thenReturn(tenantId);
        String expectedUrl = "http://localhost:8080/realms/" + tenantId;

        Uni<OidcTenantConfig> resultUni = keycloakTenantResolver.resolve(routingContext, null);
        OidcTenantConfig tenantConfig = resultUni.await().indefinitely();

        assertNotNull(tenantConfig, "TenantConfig should not be null for a valid tenant");
        assertEquals(tenantId, tenantConfig.tenantId().orElse(null));
        assertEquals(expectedUrl, tenantConfig.authServerUrl().orElse(null));
    }

    @Test
    @DisplayName("Should use cache for subsequent requests with the same tenantId")
    void testResolve_withValidTenant_shouldUseCacheOnSecondCall() {
        String tenantId = "tenant-b";
        when(httpServerRequest.getHeader("tenantId")).thenReturn(tenantId);
        when(tenantRepository.findByName(tenantId)).thenReturn(tenant);
        when(tenant.getName()).thenReturn(tenantId);
        when(tenant.getStatus()).thenReturn(Tenant.Status.ACTIVE);
        when(securityConfig.authServerUrl()).thenReturn("http://localhost:8080/realms/master");
        when(securityConfig.issuer()).thenReturn("http://localhost:8080/realms/master");
        when(securityConfig.clientId()).thenReturn(tenantId);
        String expectedUrl = "http://localhost:8080/realms/" + tenantId;

        // Act & Assert - First call (populates the cache)
        OidcTenantConfig firstResult = keycloakTenantResolver.resolve(routingContext, null).await().indefinitely();
        assertNotNull(firstResult);
        assertEquals(expectedUrl, firstResult.authServerUrl().orElse(null));

        // To prove the cache is used, we can verify the computation doesn't happen again.
        // A more advanced test could use a spy on UriBuilder, but for now, we'll ensure the
        // cache returns the correct value by checking the cache content directly.
        Map<String, OidcTenantConfig> cache = keycloakTenantResolver.getCacheForTesting();
        assertTrue(cache.containsKey(tenantId), "Cache should contain the tenantId after the first call");
        assertEquals(expectedUrl, cache.get(tenantId).authServerUrl().get());

        // Act & Assert - Second call (should hit the cache)
        OidcTenantConfig secondResult = keycloakTenantResolver.resolve(routingContext, null).await().indefinitely();
        assertNotNull(secondResult, "Second result should not be null");
        assertEquals(expectedUrl, secondResult.authServerUrl().orElse(null), "Second call should return the same URL");
        assertEquals(tenantId, secondResult.tenantId().orElse(null));
    }

    @Test
    @DisplayName("Should return null when tenantId header is missing")
    void testResolve_withNullTenantId_shouldReturnNull() {
        when(httpServerRequest.getHeader("tenantId")).thenReturn(null);

        Uni<OidcTenantConfig> resultUni = keycloakTenantResolver.resolve(routingContext, null);
        OidcTenantConfig tenantConfig = resultUni.await().indefinitely();

        assertNull(tenantConfig, "TenantConfig should be null when tenantId header is missing");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    @DisplayName("Should return null when tenantId is blank")
    void testResolve_withBlankTenantId_shouldReturnNull(String blankTenantId) {
        when(httpServerRequest.getHeader("tenantId")).thenReturn(blankTenantId);

        Uni<OidcTenantConfig> resultUni = keycloakTenantResolver.resolve(routingContext, null);
        OidcTenantConfig tenantConfig = resultUni.await().indefinitely();

        assertNull(tenantConfig, "TenantConfig should be null for blank tenantId: '" + blankTenantId + "'");
    }

    @ParameterizedTest
    @ValueSource(strings = {"tenant/a", "tenant@a", "tenant a", "<script>"})
    @DisplayName("Should return null when tenantId has invalid format")
    void testResolve_withInvalidTenantIdFormat_shouldReturnNull(String invalidTenantId) {
        when(httpServerRequest.getHeader("tenantId")).thenReturn(invalidTenantId);

        Uni<OidcTenantConfig> resultUni = keycloakTenantResolver.resolve(routingContext, null);
        OidcTenantConfig tenantConfig = resultUni.await().indefinitely();

        assertNull(tenantConfig, "TenantConfig should be null for invalid tenantId format: '" + invalidTenantId + "'");
    }
}
