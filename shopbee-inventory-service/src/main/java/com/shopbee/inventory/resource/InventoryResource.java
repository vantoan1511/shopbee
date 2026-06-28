package com.shopbee.inventory.resource;

import com.shopbee.inventory.boundary.api.InventoryApi;
import com.shopbee.inventory.dto.ConfirmReservationRequest;
import com.shopbee.inventory.dto.CreateInventoryRequest;
import com.shopbee.inventory.dto.PatchInventoryRequest;
import com.shopbee.inventory.dto.ReserveInventoryRequest;
import com.shopbee.inventory.dto.UpdateInventoryRequest;
import com.shopbee.inventory.service.InventoryService;
import com.shopbee.security.Role;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;

@RolesAllowed({Role.PLATFORM_SUPPORT, Role.PLATFORM_ADMINISTRATOR})
public class InventoryResource implements InventoryApi {

    private final InventoryService inventoryService;
    private final UriInfo uriInfo;

    public InventoryResource(InventoryService inventoryService, UriInfo uriInfo) {
        this.inventoryService = inventoryService;
        this.uriInfo = uriInfo;
    }

    @Override
    @PermitAll
    public Response getInventory(@NotNull @Size(max = 64) String tenantId, @Min(1L) Integer page, @Min(1L) @Max(100L) Integer size) {
        return Response.ok(inventoryService.getInventory(tenantId, page, size)).build();
    }

    @Override
    @PermitAll
    public Response getInventoryById(@NotNull @Size(max = 64) String tenantId, @Size(max = 64) String inventoryId) {
        return Response.ok(inventoryService.getInventoryById(tenantId, inventoryId)).build();
    }

    @Override
    public Response getInventoryAdjustments(@NotNull @Size(max = 64) String tenantId, @Size(max = 64) String inventoryId, @Min(1L) Integer page, @Min(1L) @Max(100L) Integer size) {
        return Response.ok(inventoryService.getInventoryAdjustments(tenantId, inventoryId, page, size)).build();
    }

    @Override
    public Response createInventory(@NotNull @Size(max = 64) String tenantId, @Valid @NotNull CreateInventoryRequest createInventoryRequest) {
        URI location = uriInfo.getAbsolutePathBuilder().path(inventoryService.createInventory(tenantId, createInventoryRequest)).build();
        return Response.created(location).build();
    }

    @Override
    public Response updateInventory(@NotNull @Size(max = 64) String tenantId, @Size(max = 64) String inventoryId, @Valid @NotNull UpdateInventoryRequest updateInventoryRequest) {
        inventoryService.updateInventory(tenantId, inventoryId, updateInventoryRequest);
        return Response.ok().build();
    }

    @Override
    public Response patchInventory(@NotNull @Size(max = 64) String tenantId, @Size(max = 64) String inventoryId, @Valid @NotNull PatchInventoryRequest patchInventoryRequest) {
        inventoryService.patchInventory(tenantId, inventoryId, patchInventoryRequest);
        return Response.ok().build();
    }

    @Override
    public Response deleteInventory(@NotNull @Size(max = 64) String tenantId, @Size(max = 64) String inventoryId) {
        inventoryService.deleteInventory(tenantId, inventoryId);
        return Response.noContent().build();
    }

    @Override
    public Response reserveInventory(@NotNull @Size(max = 64) String tenantId, @Valid @NotNull ReserveInventoryRequest reserveInventoryRequest) {
        URI location = uriInfo.getAbsolutePathBuilder().path(inventoryService.reserveInventory(tenantId, reserveInventoryRequest)).build();
        return Response.created(location).build();
    }

    @Override
    public Response confirmReservation(@NotNull @Size(max = 64) String tenantId, @Size(max = 64) String reservationId, @Valid @NotNull ConfirmReservationRequest confirmReservationRequest) {
        inventoryService.confirmReservation(tenantId, reservationId, confirmReservationRequest);
        return Response.ok().build();
    }

    @Override
    public Response getReservationById(@NotNull @Size(max = 64) String tenantId, @Size(max = 64) String reservationId) {
        return Response.ok(inventoryService.getReservationById(tenantId, reservationId)).build();
    }
}
