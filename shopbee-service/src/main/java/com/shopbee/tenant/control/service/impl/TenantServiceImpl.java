package com.shopbee.tenant.control.service.impl;

import com.shopbee.tenant.control.mapper.ClientMapper;
import com.shopbee.tenant.control.mapper.TenantMapper;
import com.shopbee.tenant.control.repository.ClientRepository;
import com.shopbee.tenant.control.repository.TenantRepository;
import com.shopbee.tenant.control.service.TenantService;
import com.shopbee.tenant.entity.Client;
import com.shopbee.tenant.entity.Tenant;
import com.shopbee.tenant.entity.TenantStatus;
import com.shopbee.common.exception.ConflictDataException;
import com.shopbee.common.exception.InvalidDataException;
import com.shopbee.common.exception.NotFoundException;
import com.shopbee.common.exception.UnauthorizedException;
import com.shopbee.initialization.service.InitializationConfig;
import com.shopbee.initialization.service.KeycloakConfig;
import com.shopbee.security.auth.RoleName;
import com.shopbee.tenant.model.ClientDTO;
import com.shopbee.tenant.model.CreateTenantClientRequest;
import com.shopbee.tenant.model.CreateTenantRequest;
import com.shopbee.tenant.model.TenantDTO;
import com.shopbee.tenant.model.UpdateTenantByIdRequest;
import com.shopbee.tenant.model.UpdateTenantClientByIdRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.EnumUtils;

import java.util.List;
import java.util.Objects;

@ApplicationScoped
public class TenantServiceImpl implements TenantService {
    private final TenantRepository tenantRepository;
    private final ClientRepository clientRepository;
    private final TenantMapper tenantMapper;
    private final ClientMapper clientMapper;

    private final InitializationConfig initializationConfig;
    private final KeycloakConfig keycloakConfig;

    public TenantServiceImpl(TenantRepository tenantRepository,
                             ClientRepository clientRepository,
                             TenantMapper tenantMapper,
                             ClientMapper clientMapper,
                             InitializationConfig initializationConfig,
                             KeycloakConfig keycloakConfig) {
        this.tenantRepository = tenantRepository;
        this.clientRepository = clientRepository;
        this.tenantMapper = tenantMapper;
        this.clientMapper = clientMapper;
        this.initializationConfig = initializationConfig;
        this.keycloakConfig = keycloakConfig;
    }

    @Override
    @Transactional
    public void bootstrapDefaultTenant(String adminSecret) {
        if (!initializationConfig.adminSecret().equals(adminSecret)) {
            throw new UnauthorizedException("INVALID_ADMIN_SECRET", "The provided admin secret is invalid.");
        }

        if (!tenantRepository.existsByName(keycloakConfig.defaultRealm())) {
            Tenant defaultTenant = new Tenant();
            defaultTenant.setName(keycloakConfig.defaultRealm());
            defaultTenant.setStatus(TenantStatus.ACTIVE.name());

            tenantRepository.persist(defaultTenant);

            Client applicationAdministrationClient = new Client();
            applicationAdministrationClient.setName(RoleName.APPLICATION_ADMINISTRATION);
            applicationAdministrationClient.setSecret(keycloakConfig.clients().applicationAdministration().secret());
            defaultTenant.addClient(applicationAdministrationClient);

            Client businessAdministrationClient = new Client();
            businessAdministrationClient.setName(RoleName.BUSINESS_ADMINISTRATION);
            businessAdministrationClient.setSecret(keycloakConfig.clients().businessAdministration().secret());
            defaultTenant.addClient(businessAdministrationClient);

            Client defaultClient = new Client();
            defaultClient.setName(keycloakConfig.defaultRealm() + "-application");
            defaultClient.setSecret("");
            defaultTenant.addClient(defaultClient);

            clientRepository.persist(businessAdministrationClient);
            clientRepository.persist(applicationAdministrationClient);
            clientRepository.persist(defaultClient);
        }
    }

    @Override
    public List<TenantDTO> getAllTenants(String tenantId, Integer page, Integer size) {
        return tenantRepository.findAllTenants(page - 1, size).stream()
                .map(tenantMapper::toTenantDTO)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public TenantDTO getTenantByName(String tenantId, String tenantName) {
        Tenant tenant = tenantRepository.findByName(tenantName);
        if (tenant == null) {
            throw new NotFoundException("TENANT_NOT_FOUND", "Tenant not found: " + tenantName);
        }

        return tenantMapper.toTenantDTO(tenant);
    }

    @Override
    @Transactional
    public String createTenant(String tenantId, CreateTenantRequest createTenantRequest) {
        if (tenantRepository.existsByName(createTenantRequest.getName())) {
            throw new ConflictDataException("TENANT_ALREADY_EXISTS", "Tenant already exists: " + createTenantRequest.getName());
        }

        Tenant tenant = tenantMapper.toTenant(createTenantRequest);
        tenantRepository.persist(tenant);

        return tenant.getName();
    }

    @Override
    @Transactional
    public void updateTenantById(String tenantId, String tenantName, UpdateTenantByIdRequest updateTenantByIdRequest) {
        Tenant tenant = tenantRepository.findByName(tenantName);
        if (tenant == null) {
            throw new NotFoundException("TENANT_NOT_FOUND", "Tenant not found: " + tenantName);
        }

        if (EnumUtils.getEnum(TenantStatus.class, updateTenantByIdRequest.getStatus()) == null) {
            throw new InvalidDataException("INVALID_TENANT_STATUS", "Invalid tenant status: " + updateTenantByIdRequest.getStatus());
        }

        tenantMapper.updateTenant(updateTenantByIdRequest, tenant);
    }

    @Override
    @Transactional
    public void deleteTenantById(String tenantId, String tenantName) {
        Tenant tenant = tenantRepository.findByName(tenantName);
        if (tenant == null) {
            throw new NotFoundException("TENANT_NOT_FOUND", "Tenant not found: " + tenantName);
        }

        tenantRepository.delete(tenant);
    }

    @Override
    public List<ClientDTO> getTenantClients(String tenantId, String tenantName, Integer page, Integer size) {
        return clientRepository.findByTenantName(tenantName, page - 1, size).stream()
                .map(clientMapper::toClientDTO).toList();
    }

    @Override
    public ClientDTO getTenantClientById(String tenantId, String tenantName, String clientId) {
        Client client = clientRepository.findByIdAndTenantName(clientId, tenantName);
        if (client == null) {
            throw new NotFoundException("CLIENT_NOT_FOUND", "Client not found: " + clientId);
        }

        return clientMapper.toClientDTO(client);
    }

    @Override
    @Transactional
    public String createTenantClient(String tenantId, String tenantName, CreateTenantClientRequest createTenantClientRequest) {
        Tenant tenant = tenantRepository.findByName(tenantName);
        if (tenant == null) {
            throw new NotFoundException("TENANT_NOT_FOUND", "Tenant not found: " + tenantName);
        }

        if (clientRepository.existsByNameAndTenantName(createTenantClientRequest.getClientName(), tenantName)) {
            throw new ConflictDataException("CLIENT_ALREADY_EXISTS", "Client already exists: " + createTenantClientRequest.getClientName());
        }

        Client client = clientMapper.toClient(createTenantClientRequest);
        tenant.addClient(client);
        clientRepository.persist(client);

        return client.getId();
    }

    @Override
    @Transactional
    public void updateTenantClientById(String tenantId, String tenantName, String clientId, UpdateTenantClientByIdRequest updateTenantClientByIdRequest) {
        if (clientRepository.existsByNameAndTenantNameExcludingId(updateTenantClientByIdRequest.getClientName(), tenantName, clientId)) {
            throw new ConflictDataException("CLIENT_ALREADY_EXISTS", "Client already exists: " + updateTenantClientByIdRequest.getClientName());
        }

        Client client = clientRepository.findByIdAndTenantName(clientId, tenantName);
        if (client == null) {
            throw new NotFoundException("CLIENT_NOT_FOUND", "Client not found: " + clientId);
        }

        clientMapper.updateClient(updateTenantClientByIdRequest, client);
    }

    @Override
    @Transactional
    public void deleteTenantClient(String tenantId, String tenantName, String clientId) {
        Client client = clientRepository.findByIdAndTenantName(clientId, tenantName);
        if (client == null) {
            throw new NotFoundException("CLIENT_NOT_FOUND", "Client not found: " + clientId);
        }

        clientRepository.delete(client);
    }
}
