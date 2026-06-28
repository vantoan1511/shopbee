package com.shopbee.category.control.service.impl;

import com.shopbee.category.control.mapper.CategoryMapper;
import com.shopbee.category.control.repository.CategoryRepository;
import com.shopbee.category.control.service.CategoriesService;
import com.shopbee.category.entity.Category;
import com.shopbee.category.model.CategoryDTO;
import com.shopbee.category.model.CreateCategoryRequest;
import com.shopbee.category.model.PatchCategoryByIdRequest;
import com.shopbee.category.model.UpdateCategoryByIdRequest;
import com.shopbee.common.exception.ConflictDataException;
import com.shopbee.common.exception.NotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class CategoriesServiceImpl implements CategoriesService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoriesServiceImpl(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public List<CategoryDTO> getCategories(String tenantId, List<String> code, Integer page, Integer size) {
        List<Category> categories = categoryRepository.search(tenantId, code, page - 1, size);
        return categoryMapper.toCategoryDTOs(categories);
    }

    @Override
    public CategoryDTO getCategoryById(String tenantId, String categoryId) {
        Category category = findByIdOrThrow(tenantId, categoryId);

        return categoryMapper.toCategoryDTO(category);
    }

    @Override
    @Transactional
    public String createCategory(String tenantId, CreateCategoryRequest createCategoryRequest) {
        if (categoryRepository.existsByCode(tenantId, createCategoryRequest.getCode())) {
            throw new ConflictDataException("CATEGORY_CODE_EXISTS", "Category code already exists: " + createCategoryRequest.getCode());
        }

        Category category = categoryMapper.toCategory(tenantId, createCategoryRequest);
        categoryRepository.persist(category);

        return category.getId().toString();
    }

    @Override
    @Transactional
    public void updateCategoryById(String tenantId, String categoryId, UpdateCategoryByIdRequest updateCategoryByIdRequest) {
        Category category = findByIdOrThrow(tenantId, categoryId);
        categoryMapper.updateCategory(updateCategoryByIdRequest, category);
    }

    @Override
    @Transactional
    public void patchCategoryById(String tenantId, String categoryId, PatchCategoryByIdRequest patchCategoryByIdRequest) {
        Category category = findByIdOrThrow(tenantId, categoryId);
        categoryMapper.patchCategory(patchCategoryByIdRequest, category);
    }

    @Override
    @Transactional
    public void deleteCategoryById(String tenantId, String categoryId) {
        if (!categoryRepository.deleteById(tenantId, UUID.fromString(categoryId))) {
            throw new NotFoundException("CATEGORY_NOT_FOUND", "Category not found for id: " + categoryId);
        }
    }

    private Category findByIdOrThrow(String tenantId, String categoryId) {
        Category category = categoryRepository.findById(tenantId, UUID.fromString(categoryId));
        if (category == null) {
            throw new NotFoundException("CATEGORY_NOT_FOUND", "Category not found for id: " + categoryId);
        }
        return category;
    }
}
