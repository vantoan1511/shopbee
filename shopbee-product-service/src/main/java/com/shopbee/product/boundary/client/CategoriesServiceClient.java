package com.shopbee.product.boundary.client;

import com.shopbee.category.boundary.api.CategoriesApi;
import com.shopbee.common.exception.ServiceClientExceptionMapper;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@ApplicationScoped
@RegisterRestClient(configKey = RestClientConstants.CATEGORY_SERVICE)
@RegisterProvider(ServiceClientExceptionMapper.class)
public interface CategoriesServiceClient extends CategoriesApi {
}
