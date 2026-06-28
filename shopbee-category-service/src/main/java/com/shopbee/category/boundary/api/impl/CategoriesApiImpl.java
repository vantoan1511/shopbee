package com.shopbee.category.boundary.api.impl;

import com.shopbee.category.boundary.api.CategoriesApi;
import com.shopbee.category.control.service.CategoriesService;
import com.shopbee.category.entity.CreateCategoryRequest;
import com.shopbee.category.entity.PatchCategoryByIdRequest;
import com.shopbee.category.entity.UpdateCategoryByIdRequest;
import com.shopbee.security.Role;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.List;

@RolesAllowed(Role.PLATFORM_SUPPORT)
public class CategoriesApiImpl implements CategoriesApi {

    private final CategoriesService categoriesService;
    private final UriInfo uriInfo;

    public CategoriesApiImpl(CategoriesService categoriesService, UriInfo uriInfo) {
        this.categoriesService = categoriesService;
        this.uriInfo = uriInfo;
    }

    @Override
    @PermitAll
    public Response getCategories(String tenantId, List<String> code, Integer page, Integer size) {
        return Response.ok(categoriesService.getCategories(tenantId, code, page, size)).build();
    }

    @Override
    @PermitAll
    public Response getCategoryById(String tenantId, String categoryId) {
        return Response.ok(categoriesService.getCategoryById(tenantId, categoryId)).build();
    }

    @Override
    public Response createCategory(String tenantId, CreateCategoryRequest createCategoryRequest) {
        String categoryId = categoriesService.createCategory(tenantId, createCategoryRequest);
        URI location = uriInfo.getAbsolutePathBuilder().path(categoryId).build();
        return Response.created(location).build();
    }

    @Override
    public Response updateCategoryById(String tenantId, String categoryId, UpdateCategoryByIdRequest updateCategoryByIdRequest) {
        categoriesService.updateCategoryById(tenantId, categoryId, updateCategoryByIdRequest);
        return Response.ok().build();
    }

    @Override
    public Response patchCategoryById(String tenantId, String categoryId, PatchCategoryByIdRequest patchCategoryByIdRequest) {
        categoriesService.patchCategoryById(tenantId, categoryId, patchCategoryByIdRequest);
        return Response.ok().build();
    }

    @Override
    public Response deleteCategoryById(String tenantId, String categoryId) {
        categoriesService.deleteCategoryById(tenantId, categoryId);
        return Response.noContent().build();
    }
}
