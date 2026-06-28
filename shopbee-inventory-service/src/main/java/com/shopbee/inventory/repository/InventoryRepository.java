package com.shopbee.inventory.repository;

import com.shopbee.inventory.entity.Inventory;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class InventoryRepository implements PanacheRepositoryBase<Inventory, UUID> {

    public Inventory findById(String tenantId, UUID id) {
        return find("tenantId = ?1 and id = ?2", tenantId, id).firstResult();
    }

    public boolean exists(String tenantId, String productId) {
        return count("tenantId = ?1 and productId = ?2", tenantId, productId) > 0;
    }

    public int tryReserve(String tenantId, String productId, long quantity) {
        return getEntityManager().createQuery(
                        "UPDATE Inventory i " +
                                "SET i.reservedQuantity = i.reservedQuantity + :qty " +
                                "WHERE i.tenantId = :tenantId AND i.productId = :productId " +
                                "AND :qty <= i.maxReservationQuantity " +
                                "AND (i.stockQuantity - i.reservedQuantity) >= :qty")
                .setParameter("qty", quantity)
                .setParameter("tenantId", tenantId)
                .setParameter("productId", productId)
                .executeUpdate();
    }

    public int reduceStock(String tenantId, String productId, long quantity) {
        return adjustStock(tenantId, productId, quantity, true);
    }

    public int releaseStock(String tenantId, String productId, long quantity) {
        return adjustStock(tenantId, productId, quantity, false);
    }

    private int adjustStock(String tenantId, String productId, long quantity, boolean reduceStock) {
        String query = "reservedQuantity = reservedQuantity + ?1 " + "WHERE tenantId = ?2 AND productId = ?3";
        if (reduceStock) {
            query = "stockQuantity = stockQuantity + ?1, " + query;
        }
        return update(query, quantity, tenantId, productId);
    }

}
