package com.shopbee.tenant.control.service.impl;

import com.shopbee.common.exception.ConflictResourceException;
import com.shopbee.common.exception.InvalidDataException;
import com.shopbee.common.exception.ResourceNotFoundException;
import com.shopbee.tenant.control.mapper.ClientMapper;
import com.shopbee.tenant.control.mapper.TenantMapper;
import com.shopbee.tenant.control.repository.ClientRepository;
import com.shopbee.tenant.control.repository.TenantRepository;
import com.shopbee.tenant.control.service.TenantService;
import com.shopbee.tenant.dto.ClientDTO;
import com.shopbee.tenant.dto.CreateTenantClientRequest;
import com.shopbee.tenant.dto.CreateTenantRequest;
import com.shopbee.tenant.dto.TenantDTO;
import com.shopbee.tenant.dto.UpdateTenantByIdRequest;
import com.shopbee.tenant.dto.UpdateTenantClientByIdRequest;
import com.shopbee.tenant.entity.Client;
import com.shopbee.tenant.entity.Tenant;
import com.shopbee.tenant.entity.TenantStatus;
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

    public TenantServiceImpl(TenantRepository tenantRepository,
                             ClientRepository clientRepository,
                             TenantMapper tenantMapper,
                             ClientMapper clientMapper) {
        this.tenantRepository = tenantRepository;
        this.clientRepository = clientRepository;
        this.tenantMapper = tenantMapper;
        this.clientMapper = clientMapper;
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
            throw new ResourceNotFoundException("TENANT_NOT_FOUND", "Tenant not found: " + tenantName);
        }

        return tenantMapper.toTenantDTO(tenant);
    }

    @Override
    @Transactional
    public String createTenant(String tenantId, CreateTenantRequest createTenantRequest) {
        if (tenantRepository.existsByName(createTenantRequest.getName())) {
            throw new ConflictResourceException("TENANT_ALREADY_EXISTS", "Tenant already exists: " + createTenantRequest.getName());
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
            throw new ResourceNotFoundException("TENANT_NOT_FOUND", "Tenant not found: " + tenantName);
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
            throw new ResourceNotFoundException("TENANT_NOT_FOUND", "Tenant not found: " + tenantName);
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
            throw new ResourceNotFoundException("CLIENT_NOT_FOUND", "Client not found: " + clientId);
        }

        return clientMapper.toClientDTO(client);
    }

    @Override
    @Transactional
    public String createTenantClient(String tenantId, String tenantName, CreateTenantClientRequest createTenantClientRequest) {
        Tenant tenant = tenantRepository.findByName(tenantName);
        if (tenant == null) {
            throw new ResourceNotFoundException("TENANT_NOT_FOUND", "Tenant not found: " + tenantName);
        }

        if (clientRepository.existsByNameAndTenantName(createTenantClientRequest.getClientName(), tenantName)) {
            throw new ConflictResourceException("CLIENT_ALREADY_EXISTS", "Client already exists: " + createTenantClientRequest.getClientName());
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
            throw new ConflictResourceException("CLIENT_ALREADY_EXISTS", "Client already exists: " + updateTenantClientByIdRequest.getClientName());
        }

        Client client = clientRepository.findByIdAndTenantName(clientId, tenantName);
        if (client == null) {
            throw new ResourceNotFoundException("CLIENT_NOT_FOUND", "Client not found: " + clientId);
        }

        clientMapper.updateClient(updateTenantClientByIdRequest, client);
    }

    @Override
    @Transactional
    public void deleteTenantClient(String tenantId, String tenantName, String clientId) {
        Client client = clientRepository.findByIdAndTenantName(clientId, tenantName);
        if (client == null) {
            throw new ResourceNotFoundException("CLIENT_NOT_FOUND", "Client not found: " + clientId);
        }

        clientRepository.delete(client);
    }
}
