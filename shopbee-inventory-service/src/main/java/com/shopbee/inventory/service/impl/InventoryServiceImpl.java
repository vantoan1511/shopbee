package com.shopbee.inventory.service.impl;

import com.shopbee.common.exception.ConflictResourceException;
import com.shopbee.common.exception.InvalidDataException;
import com.shopbee.common.exception.ResourceNotFoundException;
import com.shopbee.inventory.dto.ConfirmReservationRequest;
import com.shopbee.inventory.dto.CreateInventoryRequest;
import com.shopbee.inventory.dto.GetReservationById200Response;
import com.shopbee.inventory.dto.InventoryAdjustment;
import com.shopbee.inventory.dto.InventoryItem;
import com.shopbee.inventory.dto.PatchInventoryRequest;
import com.shopbee.inventory.dto.ReserveInventoryRequest;
import com.shopbee.inventory.dto.UpdateInventoryRequest;
import com.shopbee.inventory.entity.Inventory;
import com.shopbee.inventory.entity.Reservation;
import com.shopbee.inventory.mapper.InventoryAdjustmentMapper;
import com.shopbee.inventory.mapper.InventoryMapper;
import com.shopbee.inventory.mapper.ReservationMapper;
import com.shopbee.inventory.repository.InventoryAdjustmentRepository;
import com.shopbee.inventory.repository.InventoryRepository;
import com.shopbee.inventory.repository.ReservationRepository;
import com.shopbee.inventory.service.InventoryService;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryAdjustmentRepository inventoryAdjustmentRepository;
    private final ReservationRepository reservationRepository;

    private final InventoryMapper inventoryMapper;
    private final InventoryAdjustmentMapper inventoryAdjustmentMapper;
    private final ReservationMapper reservationMapper;

    private final SecurityIdentity securityIdentity;

    public InventoryServiceImpl(InventoryRepository inventoryRepository,
                                InventoryAdjustmentRepository inventoryAdjustmentRepository,
                                ReservationRepository reservationRepository,
                                InventoryMapper inventoryMapper,
                                InventoryAdjustmentMapper inventoryAdjustmentMapper,
                                ReservationMapper reservationMapper, SecurityIdentity securityIdentity) {
        this.inventoryRepository = inventoryRepository;
        this.inventoryAdjustmentRepository = inventoryAdjustmentRepository;
        this.reservationRepository = reservationRepository;
        this.inventoryMapper = inventoryMapper;
        this.inventoryAdjustmentMapper = inventoryAdjustmentMapper;
        this.reservationMapper = reservationMapper;
        this.securityIdentity = securityIdentity;
    }

    @Override
    public List<InventoryItem> getInventory(String tenantId, Integer page, Integer size) {
        return inventoryRepository.find("tenantId", tenantId).page(page - 1, size).stream()
                .map(inventoryMapper::toInventoryItem)
                .toList();
    }

    @Override
    public InventoryItem getInventoryById(String tenantId, String inventoryId) {
        Inventory found = inventoryRepository.findById(tenantId, UUID.fromString(inventoryId));
        if (found == null) {
            throw new ResourceNotFoundException("INVENTORY_NOT_FOUND", "Inventory not found " + inventoryId);
        }
        return inventoryMapper.toInventoryItem(found);
    }

    @Override
    public List<InventoryAdjustment> getInventoryAdjustments(String tenantId, String inventoryId, Integer page, Integer size) {
        return inventoryAdjustmentRepository.find("tenantId = ?1 and inventory.id = ?2", tenantId, UUID.fromString(inventoryId)).page(page - 1, size)
                .stream().map(inventoryAdjustmentMapper::toInventoryAdjustment)
                .toList();
    }

    @Override
    @Transactional
    public String createInventory(String tenantId, CreateInventoryRequest createInventoryRequest) {
        if (inventoryRepository.exists(tenantId, createInventoryRequest.getProductId())) {
            throw new ConflictResourceException("INVENTORY_EXISTED", "Inventory item existed " + createInventoryRequest.getProductId());
        }
        Inventory inventory = inventoryMapper.toInventory(tenantId, createInventoryRequest);
        inventoryRepository.persist(inventory);
        return inventory.getId().toString();
    }

    @Override
    @Transactional
    public void updateInventory(String tenantId, String inventoryId, UpdateInventoryRequest updateInventoryRequest) {
        Inventory found = inventoryRepository.findById(tenantId, UUID.fromString(inventoryId));
        if (found == null) {
            throw new ResourceNotFoundException("INVENTORY_NOT_FOUND", "Inventory not found " + inventoryId);
        }

        if (StringUtils.isNotBlank(updateInventoryRequest.getAdjustmentReason())) {
            com.shopbee.inventory.entity.InventoryAdjustment adjustment = getInventoryAdjustment(tenantId, updateInventoryRequest, found);
            inventoryAdjustmentRepository.persist(adjustment);
        }

        inventoryMapper.updateInventory(found, updateInventoryRequest);
    }

    private com.shopbee.inventory.entity.InventoryAdjustment getInventoryAdjustment(String tenantId, UpdateInventoryRequest updateInventoryRequest, Inventory found) {
        com.shopbee.inventory.entity.InventoryAdjustment adjustment = new com.shopbee.inventory.entity.InventoryAdjustment();
        adjustment.setTenantId(tenantId);
        adjustment.setReason(updateInventoryRequest.getAdjustmentReason());
        adjustment.setInventory(found);
        adjustment.setPreviousQuantity(found.getStockQuantity());
        adjustment.setNewQuantity(updateInventoryRequest.getStockQuantity());
        adjustment.setChangeQuantity(updateInventoryRequest.getStockQuantity() - found.getStockQuantity());
        adjustment.setCreatedBy(securityIdentity.getPrincipal().getName());
        return adjustment;
    }

    @Override
    @Transactional
    public void patchInventory(String tenantId, String inventoryId, PatchInventoryRequest patchInventoryRequest) {
        Inventory found = inventoryRepository.findById(tenantId, UUID.fromString(inventoryId));
        if (found == null) {
            throw new ResourceNotFoundException("INVENTORY_NOT_FOUND", "Inventory not found " + inventoryId);
        }
        inventoryMapper.patchInventory(found, patchInventoryRequest);
    }

    @Override
    @Transactional
    public void deleteInventory(String tenantId, String inventoryId) {
        inventoryRepository.delete("tenantId = ?1 and id = ?2", tenantId, inventoryId);
    }

    @Override
    @Transactional
    public String reserveInventory(String tenantId, ReserveInventoryRequest reserveInventoryRequest) {
        int updated = inventoryRepository.tryReserve(tenantId, reserveInventoryRequest.getProductId(), reserveInventoryRequest.getQuantity());
        if (updated == 0) {
            throw new InvalidDataException("INVALID_QUANTITY", "Insufficient stock quantity");
        }

        Reservation reservation = new Reservation();
        reservation.setProductId(reserveInventoryRequest.getProductId());
        reservation.setProductSku(reserveInventoryRequest.getSku());
        reservation.setQuantity(reserveInventoryRequest.getQuantity());
        reservation.setOrderId(reserveInventoryRequest.getOrderId());
        reservation.setExpiresAt(OffsetDateTime.now().plusMinutes(15));
        reservation.setTenantId(tenantId);
        reservation.setStatus("PENDING");

        reservationRepository.persist(reservation);

        return reservation.getId().toString();
    }

    @Override
    @Transactional
    public void confirmReservation(String tenantId, String reservationId, ConfirmReservationRequest confirmReservationRequest) {
        Reservation reservation = reservationRepository.find("tenantId = ?1 and id = ?2 and status = ?3", tenantId, UUID.fromString(reservationId), "PENDING").firstResult();
        if (reservation == null) {
            throw new ResourceNotFoundException("RESERVATION_NOT_FOUND", "Pending reservation not found " + reservationId);
        }
        if (StringUtils.endsWithIgnoreCase("CONFIRM", confirmReservationRequest.getAction())) {
            if (reservation.getExpiresAt().isBefore(OffsetDateTime.now())) {
                throw new InvalidDataException("RESERVATION_EXPIRED", "Reservation expired " + reservationId);
            }
            int updated = inventoryRepository.reduceStock(tenantId, reservation.getProductId(), Math.negateExact(reservation.getQuantity()));
            if (updated == 0) {
                throw new InvalidDataException("INVENTORY_NOT_FOUND", "Inventory not found " + reservation.getProductId());
            }
            reservation.setStatus("CONFIRMED");
        } else if (StringUtils.endsWithIgnoreCase("RELEASE", confirmReservationRequest.getAction())) {
            int updated = inventoryRepository.releaseStock(tenantId, reservation.getProductId(), Math.negateExact(reservation.getQuantity()));
            if (updated == 0) {
                throw new InvalidDataException("INVENTORY_NOT_FOUND", "Inventory not found " + reservation.getProductId());
            }
            reservation.setStatus("CANCELLED");
        }
    }

    @Override
    public GetReservationById200Response getReservationById(String tenantId, String reservationId) {
        Reservation found = reservationRepository.find("tenantId = ?1 and id = ?2", tenantId, UUID.fromString(reservationId)).firstResult();
        if (found == null) {
            throw new ResourceNotFoundException("RESERVATION_NOT_FOUND", "Reservation not found " + reservationId);
        }
        return reservationMapper.toGetReservationById200Response(found);
    }
}
