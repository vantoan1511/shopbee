package com.shopbee.initialization.service;

import com.shopbee.security.context.TenantContextExecutor;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

@ApplicationScoped
public class ApplicationInitializationService {
    private static final Logger LOG = LoggerFactory.getLogger(ApplicationInitializationService.class);
    private final KeycloakInitializationService keycloakInitializationService;
    private final InitializationConfig initializationConfig;
    private final KeycloakConfig keycloakConfig;
    private final RemoteTenantService remoteTenantService;

    public ApplicationInitializationService(KeycloakInitializationService keycloakInitializationService,
                                            InitializationConfig initializationConfig,
                                            KeycloakConfig keycloakConfig,
                                            @RestClient RemoteTenantService remoteTenantService) {
        this.keycloakInitializationService = keycloakInitializationService;
        this.initializationConfig = initializationConfig;
        this.keycloakConfig = keycloakConfig;
        this.remoteTenantService = remoteTenantService;
    }

    void initialize(@Observes StartupEvent event) {
        CompletableFuture.runAsync(() -> TenantContextExecutor.getInstance().runWithTenant(keycloakConfig.defaultRealm(), this::initializeApplication))
                .thenRun(() -> LOG.info("Application initialization completed."))
                .exceptionally(ex -> {
                    LOG.error("Application initialization failed: {}", ex.getMessage(), ex);
                    return null;
                });
    }

    private void initializeApplication() {
        if (initializationConfig.keycloak()) {
            bootstrapServices();
            keycloakInitializationService.initialize();
            LOG.info("Keycloak initialized successfully.");
        }
    }

    private void bootstrapServices() {
        for (int i = 0; i < 10; i++) {
            try (Response bootstrapDefaultTenantResponse = remoteTenantService.bootstrapDefaultTenant(initializationConfig.adminSecret())) {
                if (bootstrapDefaultTenantResponse.getStatus() == Response.Status.CONFLICT.getStatusCode()) {
                    LOG.info("Default tenant already exists. Skipping bootstrap.");
                    return;
                }

                if (bootstrapDefaultTenantResponse.getStatus() == Response.Status.OK.getStatusCode()) {
                    LOG.info("Default tenant bootstrapped successfully.");
                    return;
                }
            } catch (ProcessingException e) {
                try {
                    LOG.warn("Tenant service is not available yet {}. Retrying... ({}/10)", e.getMessage(), i + 1);
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    // Ignore
                }
            }
        }
    }
}
