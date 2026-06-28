package com.shopbee.product.control.repository;

import com.shopbee.product.entity.Product;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ProductRepository implements PanacheRepositoryBase<Product, UUID> {

    public List<Product> findAll(String tenantId, int page, int size) {
        return find("tenantId", tenantId).page(page, size).list();
    }

    public Product findById(String tenantId, UUID id) {
        return find("tenantId = ?1 AND id = ?2", tenantId, id).firstResult();
    }

    public boolean existsBySku(String tenantId, String sku) {
        return count("tenantId = ?1 AND sku = ?2", tenantId, sku) > 0;
    }
}
