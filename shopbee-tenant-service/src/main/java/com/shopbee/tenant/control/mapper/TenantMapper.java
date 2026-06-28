package com.shopbee.tenant.control.mapper;

import com.shopbee.tenant.dto.CreateTenantRequest;
import com.shopbee.tenant.dto.TenantDTO;
import com.shopbee.tenant.dto.UpdateTenantByIdRequest;
import com.shopbee.tenant.entity.Tenant;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TenantMapper {

    Tenant toTenant(TenantDTO tenantDTO);

    @Mapping(target = "status", constant = "ACTIVE")
    Tenant toTenant(CreateTenantRequest createTenantRequest);

    List<TenantDTO> toTenantDTOs(List<Tenant> tenants);

    TenantDTO toTenantDTO(Tenant tenant);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateTenant(UpdateTenantByIdRequest updateTenantByIdRequest, @MappingTarget Tenant tenant);
}
