package com.shopbee.order.control.repository;

import com.shopbee.order.entity.Order;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class OrderRepository implements PanacheRepository<Order> {

    public List<Order> findByUserId(String tenantId, String userId, int page, int size) {
        return find("tenantId = ?1 AND userId = ?2", tenantId, userId).page(page, size).list();
    }

    public Order findById(String tenantId, String id) {
        return find("tenantId = ?1 AND id = ?2", tenantId, id).firstResult();
    }
}
