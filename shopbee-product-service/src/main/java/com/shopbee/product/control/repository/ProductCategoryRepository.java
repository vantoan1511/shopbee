package com.shopbee.product.control.repository;

import com.shopbee.product.entity.ProductCategory;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ProductCategoryRepository implements PanacheRepositoryBase<ProductCategory, UUID> {

    public List<ProductCategory> findByProductIdAndTenantId(String tenantId, UUID productId, int page, int size) {
        return find("productId = ?1 and tenantId = ?2", productId, tenantId).page(page, size).list();
    }

    public boolean existsByTenantIdAndProductIdAndCategoryId(String tenantId, UUID productId, UUID categoryId) {
        return count("productId = ?1 and categoryId = ?2 and tenantId = ?3", productId, categoryId, tenantId) > 0;
    }

    public void deleteByTenantIdAndProductIdAndCategoryId(String tenantId, UUID productId, UUID categoryId) {
        delete("productId = ?1 and categoryId = ?2 and tenantId = ?3", productId, categoryId, tenantId);
    }

}
