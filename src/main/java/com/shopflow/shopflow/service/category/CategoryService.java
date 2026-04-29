package com.shopflow.shopflow.service.category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.shopflow.shopflow.dto.category.CategoryResponse;
import com.shopflow.shopflow.dto.category.CreateCategoryRequest;
import com.shopflow.shopflow.dto.category.UpdateCategoryRequest;

public interface CategoryService {
    Page<CategoryResponse> findAll(Pageable pageable);

    CategoryResponse findById(Long id);

    CategoryResponse createCategory(CreateCategoryRequest category);

    CategoryResponse updateCategory(Long id, UpdateCategoryRequest category);

    void deleteCategory(Long id);
}
