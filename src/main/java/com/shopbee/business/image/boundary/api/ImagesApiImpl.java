/*
 * ImagesApiImpl.java
 *
 * Copyright by shopbee-service, all rights reserved.
 * MIT License: https://mit-license.org
 */

package com.shopbee.business.image.boundary.api;

import com.shopbee.business.image.control.service.ImagesService;
import com.shopbee.image.boundary.api.ImagesApi;
import com.shopbee.image.model.UploadImageRequest;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;

public class ImagesApiImpl implements ImagesApi {

    private final ImagesService imagesService;
    private final UriInfo uriInfo;

    @Inject
    public ImagesApiImpl(ImagesService imagesService, UriInfo uriInfo) {
        this.imagesService = imagesService;
        this.uriInfo = uriInfo;
    }

    @Override
    public Response getImages(String tenantId, Integer page, Integer size) {
        return Response.ok(imagesService.getImages(tenantId, page, size)).build();
    }

    @Override
    public Response getImageById(String tenantId, String imageId) {
        return Response.ok(imagesService.getImageById(tenantId, imageId)).build();
    }

    @Override
    public Response uploadImage(String tenantId, UploadImageRequest uploadImageRequest) {
        URI location = uriInfo.getAbsolutePathBuilder().path(imagesService.uploadImage(tenantId, uploadImageRequest)).build();
        return Response.created(location).build();
    }

    @Override
    public Response deleteImageById(String tenantId, String imageId) {
        imagesService.deleteImageById(tenantId, imageId);
        return Response.noContent().build();
    }
}
