package com.shopbee.category.control.repository;

import com.shopbee.category.entity.Category;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class CategoryRepository implements PanacheRepositoryBase<Category, UUID> {

    public List<Category> search(String tenantId, List<String> code, int page, int size) {
        return find("tenantId = ?1 AND code in ?2", tenantId, code).page(page, size).list();
    }

    public Category findById(String tenantId, UUID categoryId) {
        return find("tenantId = ?1 AND id = ?2", tenantId, categoryId).firstResult();
    }

    public boolean existsByCode(String tenantId, String code) {
        return count("tenantId = ?1 AND code = ?2", tenantId, code) > 0;
    }

    public boolean deleteById(String tenantId, UUID categoryId) {
        return delete("tenantId = ?1 AND id = ?2", tenantId, categoryId) > 0;
    }
}
