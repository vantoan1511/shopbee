/*
 * ImageMapper.java
 *
 * Copyright by shopbee-service, all rights reserved.
 * MIT License: https://mit-license.org
 */

package com.shopbee.business.image.control.mapper;

import com.shopbee.business.image.entity.Image;
import com.shopbee.image.model.GetImages200ResponseInner;
import com.shopbee.image.model.ImageDTO;
import com.shopbee.image.model.UploadImageRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ImageMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "tenantId", source = "tenantId")
    Image toImage(String tenantId, UploadImageRequest source);

    ImageDTO toImageDTO(Image source);

    @Mapping(target = "data", ignore = true)
    GetImages200ResponseInner toGetImagesResponseInner(Image source);

    List<GetImages200ResponseInner> toGetImagesResponseInners(List<Image> source);

}
