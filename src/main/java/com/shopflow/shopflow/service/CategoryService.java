package com.shopflow.shopflow.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.shopflow.shopflow.dto.CategoryResponse;
import com.shopflow.shopflow.dto.CreateCategoryRequest;
import com.shopflow.shopflow.dto.UpdateCategoryRequest;

public interface CategoryService {
    Page<CategoryResponse> findAll(Pageable pageable);

    CategoryResponse findById(Long id);

    CategoryResponse createCategory(CreateCategoryRequest category);

    CategoryResponse updateCategory(Long id, UpdateCategoryRequest category);

    void deleteCategory(Long id);
}
