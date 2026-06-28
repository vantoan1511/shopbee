package com.shopbee.tenant;

import com.shopbee.tenant.control.repository.ClientRepository;
import com.shopbee.tenant.control.repository.TenantRepository;
import com.shopbee.tenant.entity.Client;
import com.shopbee.tenant.entity.Tenant;
import com.shopbee.tenant.entity.TenantStatus;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class TenantBootstrap {
    private static final Logger LOG = LoggerFactory.getLogger(TenantBootstrap.class);
    private final TenantRepository tenantRepository;
    private final ClientRepository clientRepository;
    private final BootstrapConfig config;

    public TenantBootstrap(TenantRepository tenantRepository,
                           ClientRepository clientRepository,
                           BootstrapConfig config) {
        this.tenantRepository = tenantRepository;
        this.clientRepository = clientRepository;
        this.config = config;
    }

    @Transactional
    void onStartUp(@Observes StartupEvent event) {
        if (tenantRepository.existsByName(config.defaultTenant())) {
            LOG.debug("Default tenant '{}' already exists, skipping initialization.", config.defaultTenant());
            return;
        }

        Tenant defaultTenant = new Tenant();
        defaultTenant.setName(config.defaultTenant());
        defaultTenant.setStatus(TenantStatus.ACTIVE.name());

        tenantRepository.persist(defaultTenant);

        config.defaults().client().forEach((clientName, clientConfig) -> {
            Client client = new Client();
            client.setName(clientName);
            client.setSecret(clientConfig.secret());
            defaultTenant.addClient(client);
            clientRepository.persist(client);
        });

        LOG.info("Default tenant initialization completed.");
    }
}
