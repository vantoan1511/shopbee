package com.shopbee.tenant.control.mapper;

import com.shopbee.tenant.dto.ClientDTO;
import com.shopbee.tenant.dto.CreateTenantClientRequest;
import com.shopbee.tenant.dto.UpdateTenantClientByIdRequest;
import com.shopbee.tenant.entity.Client;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI, unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface ClientMapper {

    @Mapping(target = "clientId", source = "id")
    @Mapping(target = "clientName", source = "name")
    @Mapping(target = "clientSecret", source = "secret")
    ClientDTO toClientDTO(Client client);

    @Mapping(target = "name", source = "clientName")
    @Mapping(target = "secret", source = "clientSecret")
    Client toClient(CreateTenantClientRequest createTenantClientRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
    @Mapping(target = "name", source = "clientName")
    @Mapping(target = "secret", source = "clientSecret")
    void updateClient(UpdateTenantClientByIdRequest updateTenantClientByIdRequest, @MappingTarget Client client);

}
