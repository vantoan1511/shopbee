package com.shopbee.tenant.control.service;

import com.shopbee.tenant.dto.ClientDTO;
import com.shopbee.tenant.dto.CreateTenantClientRequest;
import com.shopbee.tenant.dto.CreateTenantRequest;
import com.shopbee.tenant.dto.TenantDTO;
import com.shopbee.tenant.dto.UpdateTenantByIdRequest;
import com.shopbee.tenant.dto.UpdateTenantClientByIdRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.DefaultValue;

import java.util.List;

public interface TenantService {
    List<TenantDTO> getAllTenants(@NotNull @Size(max = 64) String tenantId, @Min(1) @DefaultValue("1") Integer page, @Min(1) @Max(100) @DefaultValue("20") Integer size);

    TenantDTO getTenantByName(@Size(max = 64) @NotNull String tenantId, @NotNull String tenantName);

    String createTenant(@Size(max = 64) @NotNull String tenantId, @Valid @NotNull CreateTenantRequest createTenantRequest);

    void updateTenantById(@Size(max = 64) @NotNull String tenantId, @NotNull String tenantName, @Valid @NotNull UpdateTenantByIdRequest updateTenantByIdRequest);

    void deleteTenantById(@Size(max = 64) @NotNull String tenantId, @NotNull String tenantName);

    List<ClientDTO> getTenantClients(@NotNull @Size(max = 64) String tenantId, @NotNull String tenantName, @Min(0) @DefaultValue("0") Integer page, @Min(1) @Max(100) @DefaultValue("20") Integer size);

    ClientDTO getTenantClientById(@NotNull @Size(max = 64) String tenantId, @NotNull String tenantName, String clientId);

    String createTenantClient(@NotNull @Size(max = 64) String tenantId, @NotNull String tenantName, @Valid @NotNull CreateTenantClientRequest createTenantClientRequest);

    void updateTenantClientById(@NotNull @Size(max = 64) String tenantId, @NotNull String tenantName, String clientId, @Valid @NotNull UpdateTenantClientByIdRequest updateTenantClientByIdRequest);

    void deleteTenantClient(@NotNull @Size(max = 64) String tenantId, @NotNull String tenantName, String clientId);
}
