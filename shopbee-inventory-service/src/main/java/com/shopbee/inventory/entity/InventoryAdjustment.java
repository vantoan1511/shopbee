package com.shopbee.inventory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "shopbee_inventory_adjustments")
public class InventoryAdjustment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JoinColumn(name = "inventory_id")
    @ManyToOne
    private Inventory inventory;

    @Column(name = "previous_quantity")
    private long previousQuantity;

    @Column(name = "new_quantity")
    private long newQuantity;

    @Column(name = "change_quantity")
    private long changeQuantity;

    @Column
    private String reason;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at")
    @CreationTimestamp
    private OffsetDateTime createdAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public long getPreviousQuantity() {
        return previousQuantity;
    }

    public void setPreviousQuantity(long previousQuantity) {
        this.previousQuantity = previousQuantity;
    }

    public long getNewQuantity() {
        return newQuantity;
    }

    public void setNewQuantity(long newQuantity) {
        this.newQuantity = newQuantity;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        InventoryAdjustment that = (InventoryAdjustment) o;
        return previousQuantity == that.previousQuantity && newQuantity == that.newQuantity && Objects.equals(id, that.id) && Objects.equals(inventory, that.inventory) && Objects.equals(reason, that.reason) && Objects.equals(createdBy, that.createdBy) && Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, inventory, previousQuantity, newQuantity, reason, createdBy, createdAt);
    }

    public long getChangeQuantity() {
        return changeQuantity;
    }

    public void setChangeQuantity(long changeQuantity) {
        this.changeQuantity = changeQuantity;
    }
}
