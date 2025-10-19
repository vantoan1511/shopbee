package com.shopbee.product.control.repository;

import com.shopbee.product.entity.Product;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class ProductRepository implements PanacheRepository<Product> {

    public List<Product> findAll(String tenantId, int page, int size) {
        return find("tenantId", tenantId).page(page, size).list();
    }

    public Product findById(String tenantId, String id) {
        return find("tenantId = ?1 AND id = ?2", tenantId, id).firstResult();
    }

    public Product findBySku(String tenantId, String sku) {
        return find("tenantId = ?1 AND sku = ?2", tenantId, sku).firstResult();
    }

    public long countBySku(String tenantId, String sku) {
        return count("tenantId = ?1 AND sku = ?2", tenantId, sku);
    }

    public long countBySkuExcludeProductId(String tenantId, String sku, String excludeProductId) {
        return count("tenantId = ?1 AND sku = ?2 AND id <> ?3", tenantId, sku, excludeProductId);
    }
}
