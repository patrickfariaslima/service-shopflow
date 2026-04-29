package com.shopflow.shopflow.service.category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.shopflow.shopflow.dto.category.CategoryResponse;
import com.shopflow.shopflow.dto.category.CreateCategoryRequest;
import com.shopflow.shopflow.dto.category.UpdateCategoryRequest;
import com.shopflow.shopflow.entity.CategoryEntity;
import com.shopflow.shopflow.exception.ResourceNotFoundException;
import com.shopflow.shopflow.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private String categoryNotFoundMessage = "Category not found with id: ";

    @Override
    public Page<CategoryResponse> findAll(Pageable pageable) {
        return categoryRepository.findByActiveTrue(pageable)
                .map(entity -> CategoryResponse.builder()
                        .id(entity.getId())
                        .name(entity.getName())
                        .description(entity.getDescription())
                        .active(entity.isActive())
                        .build());
    }

    @Override
    public CategoryResponse findById(Long id) {
        return categoryRepository.findByIdAndActiveTrue(id)
                .map(entity -> CategoryResponse.builder()
                        .id(entity.getId())
                        .name(entity.getName())
                        .description(entity.getDescription())
                        .active(entity.isActive())
                        .build()
                ).orElseThrow(() -> new ResourceNotFoundException(categoryNotFoundMessage + id));
    }

    @Override
    public void deleteCategory(Long id) {
        CategoryEntity category = categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(categoryNotFoundMessage + id));
        category.setActive(false);

        categoryRepository.save(category);
    }

    @Override
    public CategoryResponse createCategory(CreateCategoryRequest category) {
        CategoryEntity newCategory = CategoryEntity.builder()
                .name(category.getName())
                .description(category.getDescription())
                .active(true)
                .build();
        CategoryEntity savedCategory = categoryRepository.save(newCategory);

        return CategoryResponse.builder()
                .id(savedCategory.getId())
                .name(savedCategory.getName())
                .description(savedCategory.getDescription())
                .active(savedCategory.isActive())
                .build();
    }

    @Override
    public CategoryResponse updateCategory(Long id, UpdateCategoryRequest category) {
        CategoryEntity entity = categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(categoryNotFoundMessage + id));

        if (category.getName() != null) {
            entity.setName(category.getName());
        }

        if (category.getDescription() != null) {
            entity.setDescription(category.getDescription());
        }

        if (category.getActive() != null) {
            entity.setActive(category.getActive());
        }

        CategoryEntity updatedEntity = categoryRepository.save(entity);

        return CategoryResponse.builder()
                .id(updatedEntity.getId())
                .name(updatedEntity.getName())
                .description(updatedEntity.getDescription())
                .active(updatedEntity.isActive())
                .build();
    }
}
