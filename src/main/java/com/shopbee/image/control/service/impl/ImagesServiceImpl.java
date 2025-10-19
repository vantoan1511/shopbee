/*
 * ImagesServiceImpl.java
 *
 * Copyright by shopbee-service, all rights reserved.
 * MIT License: https://mit-license.org
 */

package com.shopbee.image.control.service.impl;

import com.shopbee.common.exception.ApiServiceException;
import com.shopbee.image.control.mapper.ImageMapper;
import com.shopbee.image.control.repository.ImagesRepository;
import com.shopbee.image.control.service.ImagesService;
import com.shopbee.image.entity.Image;
import com.shopbee.image.model.GetImages200ResponseInner;
import com.shopbee.image.model.ImageDTO;
import com.shopbee.image.model.UploadImageRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@ApplicationScoped
public class ImagesServiceImpl implements ImagesService {

    private static final Logger LOG = LoggerFactory.getLogger(ImagesServiceImpl.class);
    private final ImagesRepository imagesRepository;

    private final ImageMapper imageMapper;

    @Inject
    public ImagesServiceImpl(ImagesRepository imagesRepository, ImageMapper imageMapper) {
        this.imagesRepository = imagesRepository;
        this.imageMapper = imageMapper;
    }

    @Override
    public List<GetImages200ResponseInner> getImages(String tenantId, Integer page, Integer size) {
        List<Image> images = imagesRepository.find(tenantId, page, size);
        return imageMapper.toGetImagesResponseInners(images);
    }

    @Override
    public ImageDTO getImageById(String tenantId, String imageId) {
        Image image = findImageById(tenantId, imageId);
        return imageMapper.toImageDTO(image);
    }

    @Override
    @Transactional
    public String uploadImage(String tenantId, UploadImageRequest uploadImageRequest) {
        validateUploadImageRequest(uploadImageRequest);

        Image image = imageMapper.toImage(tenantId, uploadImageRequest);
        imagesRepository.persist(image);

        return image.getId();
    }

    @Override
    @Transactional
    public void deleteImageById(String tenantId, String imageId) {
        imagesRepository.deleteById(tenantId, imageId);
    }

    private Image findImageById(String tenantId, String imageId) {
        Image image = imagesRepository.findById(tenantId, imageId);

        if (image == null) {
            throw ApiServiceException.notFound("Image [{}] not found", imageId);
        }

        return image;
    }

    private void validateUploadImageRequest(UploadImageRequest uploadImageRequest) {
        if (StringUtils.isBlank(uploadImageRequest.getData())) {
            throw ApiServiceException.badRequest("Image data must not be blank");
        }
        if (StringUtils.isBlank(uploadImageRequest.getFileName())) {
            throw ApiServiceException.badRequest("Image file name must not be blank");
        }
    }
}
