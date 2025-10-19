/*
 * ImagesRepository.java
 *
 * Copyright by shopbee-service, all rights reserved.
 * MIT License: https://mit-license.org
 */

package com.shopbee.business.image.control.repository;

import com.shopbee.business.image.entity.Image;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class ImagesRepository implements PanacheRepositoryBase<Image, String> {

    public List<Image> find(String tenantId, int page, int size) {
        return find("tenantId", tenantId).page(page, size).list();
    }

    public Image findById(String tenantId, String imageId) {
        return find("tenantId = ?1 AND imageId = ?2", tenantId, imageId).firstResult();
    }

    public void deleteById(String tenantId, String imageId) {
        delete("tenantId = ?1 AND imageId = ?2", tenantId, imageId);
    }
}
