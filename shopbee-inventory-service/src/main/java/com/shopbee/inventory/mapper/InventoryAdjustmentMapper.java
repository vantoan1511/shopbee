package com.shopbee.inventory.mapper;

import com.shopbee.inventory.dto.InventoryAdjustment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface InventoryAdjustmentMapper {

    @Mapping(target = "inventoryId", source = "inventory.id")
    InventoryAdjustment toInventoryAdjustment(com.shopbee.inventory.entity.InventoryAdjustment inventoryAdjustment);

}
