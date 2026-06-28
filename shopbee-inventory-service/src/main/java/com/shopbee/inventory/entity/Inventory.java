package com.shopbee.inventory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "shopbee_inventory", uniqueConstraints = {@UniqueConstraint(columnNames = "tenant_id, product_id")})
public class Inventory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "product_id")
    private String productId;

    @Column(name = "sku")
    private String sku;

    @Column
    private boolean active = true;

    @Column(name = "stock_quantity")
    private long stockQuantity = 0;

    @Column(name = "max_reservation_quantity")
    private long maxReservationQuantity = 0;

    @Column(name = "reserved_quantity")
    private long reservedQuantity = 0;

    @Version
    private Long version;

    @Column(name = "created_at")
    @CreationTimestamp
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private OffsetDateTime updatedAt;

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public long getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(long stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public long getReservedQuantity() {
        return reservedQuantity;
    }

    public void setReservedQuantity(long reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public long getMaxReservationQuantity() {
        return maxReservationQuantity;
    }

    public void setMaxReservationQuantity(long maxReservationQuantity) {
        this.maxReservationQuantity = maxReservationQuantity;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Inventory inventory = (Inventory) o;

        return stockQuantity == inventory.stockQuantity && reservedQuantity == inventory.reservedQuantity && version == inventory.version && Objects.equals(id, inventory.id) && Objects.equals(productId, inventory.productId) && Objects.equals(createdAt, inventory.createdAt) && Objects.equals(updatedAt, inventory.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, productId, stockQuantity, reservedQuantity, version, createdAt, updatedAt);
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }
}
