package com.shopbee.product.boundary.client;

import com.shopbee.RestClientConstants;
import com.shopbee.category.boundary.api.CategoriesApi;
import com.shopbee.common.exception.ClientExceptionMapper;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@ApplicationScoped
@RegisterRestClient(configKey = RestClientConstants.CATEGORIES_SERVICE)
@RegisterProvider(ClientExceptionMapper.class)
public interface CategoriesServiceClient extends CategoriesApi {
}
