package com.shopbee.product.boundary.client;

import com.shopbee.category.entity.CategoryDTO;
import com.shopbee.common.client.rest.ServiceRestClientAdapter;
import com.shopbee.common.exception.BusinessException;
import com.shopbee.common.exception.TechnicalException;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class CategoriesServiceClientAdapter extends ServiceRestClientAdapter {

    private final CategoriesServiceClient categoriesServiceClient;

    public CategoriesServiceClientAdapter(@RestClient CategoriesServiceClient categoriesServiceClient) {
        this.categoriesServiceClient = categoriesServiceClient;
    }

    @Retry(maxRetries = 2, retryOn = TechnicalException.class, abortOn = BusinessException.class)
    @CircuitBreaker(requestVolumeThreshold = 5, failureRatio = 0.6, skipOn = BusinessException.class)
    public CategoryDTO getCategoryById(String tenantId, String categoryId) {
        return handle(() -> categoriesServiceClient.getCategoryById(tenantId, categoryId), CategoryDTO.class);
    }

    @Override
    protected String getServiceName() {
        return RestClientConstants.CATEGORY_SERVICE;
    }
}
