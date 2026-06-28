package com.shopbee.inventory.mapper;

import com.shopbee.inventory.dto.CreateInventoryRequest;
import com.shopbee.inventory.dto.InventoryItem;
import com.shopbee.inventory.dto.PatchInventoryRequest;
import com.shopbee.inventory.dto.UpdateInventoryRequest;
import com.shopbee.inventory.entity.Inventory;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface InventoryMapper {

    @Mapping(target = "tenantId", source = "tenantId")
    Inventory toInventory(String tenantId, CreateInventoryRequest createInventoryRequest);

    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "active", defaultValue = "true")
    @Mapping(target = "stockQuantity", defaultValue = "0l")
    @Mapping(target = "maxReservationQuantity", defaultValue = "0l")
    @Mapping(target = "reservedQuantity", defaultValue = "0l")
    void updateInventory(@MappingTarget Inventory inventory, UpdateInventoryRequest updateInventoryRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void patchInventory(@MappingTarget Inventory inventory, PatchInventoryRequest patchInventoryRequest);

    @Mapping(target = "availableQuantity", source = "inventory", qualifiedByName = "getAvailableQuantity")
    InventoryItem toInventoryItem(Inventory inventory);

    @Named("getAvailableQuantity")
    default Integer getAvailableQuantity(Inventory inventory) {
        if (inventory == null) {
            return null;
        }
        return Math.toIntExact(inventory.getStockQuantity() - inventory.getReservedQuantity());
    }
}
