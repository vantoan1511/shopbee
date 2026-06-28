package com.shopbee.inventory.service;

import com.shopbee.inventory.dto.ConfirmReservationRequest;
import com.shopbee.inventory.dto.CreateInventoryRequest;
import com.shopbee.inventory.dto.GetReservationById200Response;
import com.shopbee.inventory.dto.InventoryAdjustment;
import com.shopbee.inventory.dto.InventoryItem;
import com.shopbee.inventory.dto.PatchInventoryRequest;
import com.shopbee.inventory.dto.ReserveInventoryRequest;
import com.shopbee.inventory.dto.UpdateInventoryRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public interface InventoryService {

    List<InventoryItem> getInventory(@NotNull @Size(max = 64) String tenantId, @Min(1L) Integer page, @Min(1L) @Max(100L) Integer size);

    InventoryItem getInventoryById(@NotNull @Size(max = 64) String tenantId, @Size(max = 64) String inventoryId);

    List<InventoryAdjustment> getInventoryAdjustments(@NotNull @Size(max = 64) String tenantId, @Size(max = 64) String inventoryId, @Min(1L) Integer page, @Min(1L) @Max(100L) Integer size);

    String createInventory(@NotNull @Size(max = 64) String tenantId, @Valid @NotNull CreateInventoryRequest createInventoryRequest);

    void updateInventory(@NotNull @Size(max = 64) String tenantId, @Size(max = 64) String inventoryId, @Valid @NotNull UpdateInventoryRequest updateInventoryRequest);

    void patchInventory(@NotNull @Size(max = 64) String tenantId, @Size(max = 64) String inventoryId, @Valid @NotNull PatchInventoryRequest patchInventoryRequest);

    void deleteInventory(@NotNull @Size(max = 64) String tenantId, @Size(max = 64) String inventoryId);

    String reserveInventory(@NotNull @Size(max = 64) String tenantId, @Valid @NotNull ReserveInventoryRequest reserveInventoryRequest);

    void confirmReservation(@NotNull @Size(max = 64) String tenantId, @Size(max = 64) String reservationId, @Valid @NotNull ConfirmReservationRequest confirmReservationRequest);

    GetReservationById200Response getReservationById(@NotNull @Size(max = 64) String tenantId, @Size(max = 64) String reservationId);
}
