package com.shopbee.image.control.service;

import com.shopbee.image.model.GetImages200ResponseInner;
import com.shopbee.image.model.ImageDTO;
import com.shopbee.image.model.UploadImageRequest;

import java.util.List;

public interface ImagesService {

    List<GetImages200ResponseInner> getImages(String tenantId, Integer page, Integer size);

    ImageDTO getImageById(String tenantId, String imageId);

    String uploadImage(String tenantId, UploadImageRequest uploadImageRequest);

    void deleteImageById(String tenantId, String imageId);
}
