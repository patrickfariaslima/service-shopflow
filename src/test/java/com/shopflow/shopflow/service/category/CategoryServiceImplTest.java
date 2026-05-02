package com.shopflow.shopflow.service.category;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.shopflow.shopflow.dto.category.CategoryResponse;
import com.shopflow.shopflow.dto.category.CreateCategoryRequest;
import com.shopflow.shopflow.dto.category.UpdateCategoryRequest;
import com.shopflow.shopflow.entity.CategoryEntity;
import com.shopflow.shopflow.exception.ResourceNotFoundException;
import com.shopflow.shopflow.repository.CategoryRepository;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void findById_WhenCategoryExists_ShouldReturnResponse() {
        CategoryEntity category = CategoryEntity.builder()
                .id(1L).name("Electronics").description("Electronic products").active(true).build();

        when(categoryRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(category));

        CategoryResponse response = categoryService.findById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Electronics", response.getName());
    }

    @Test
    void findById_WhenCategoryNotFound_ShouldThrowResourceNotFoundException() {
        when(categoryRepository.findByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.findById(99L));
    }

    @Test
    void createCategory_ShouldPersistAndReturnResponse() {
        CreateCategoryRequest request = new CreateCategoryRequest("Electronics", "Electronic products");

        CategoryEntity savedEntity = CategoryEntity.builder()
                .id(1L).name("Electronics").description("Electronic products").active(true).build();

        when(categoryRepository.save(any(CategoryEntity.class))).thenReturn(savedEntity);

        CategoryResponse response = categoryService.createCategory(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Electronics", response.getName());
        verify(categoryRepository).save(any(CategoryEntity.class));
    }

    @Test
    void deleteCategory_WhenCategoryExists_ShouldDeactivate() {
        CategoryEntity category = CategoryEntity.builder()
                .id(1L).name("Electronics").active(true).build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        categoryService.deleteCategory(1L);

        assertFalse(category.isActive());
        verify(categoryRepository).save(category);
    }

    @Test
    void deleteCategory_WhenCategoryNotFound_ShouldThrowResourceNotFoundException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.deleteCategory(99L));
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void updateCategory_WhenCategoryExists_ShouldUpdateFields() {
        CategoryEntity category = CategoryEntity.builder()
                .id(1L).name("Old Name").description("Old Desc").active(true).build();
        UpdateCategoryRequest request = new UpdateCategoryRequest("New Name", "New Desc", null);

        CategoryEntity savedEntity = CategoryEntity.builder()
                .id(1L).name("New Name").description("New Desc").active(true).build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(category)).thenReturn(savedEntity);

        CategoryResponse response = categoryService.updateCategory(1L, request);

        assertEquals("New Name", response.getName());
        assertEquals("New Desc", response.getDescription());
        verify(categoryRepository).save(category);
    }

    @Test
    void updateCategory_WhenCategoryNotFound_ShouldThrowResourceNotFoundException() {
        UpdateCategoryRequest request = new UpdateCategoryRequest("New Name", null, null);

        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.updateCategory(99L, request));
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void findAll_ShouldReturnMappedPage() {
        CategoryEntity category = CategoryEntity.builder()
                .id(1L).name("Electronics").description("Electronic products").active(true).build();
        Pageable pageable = PageRequest.of(0, 12);

        when(categoryRepository.findByActiveTrue(pageable)).thenReturn(new PageImpl<>(List.of(category)));

        Page<CategoryResponse> result = categoryService.findAll(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Electronics", result.getContent().get(0).getName());
        assertTrue(result.getContent().get(0).isActive());
    }

    @Test
    void updateCategory_WhenActiveFieldChanged_ShouldUpdateActiveStatus() {
        CategoryEntity category = CategoryEntity.builder()
                .id(1L).name("Electronics").description("Old Desc").active(true).build();
        UpdateCategoryRequest request = new UpdateCategoryRequest(null, null, false);

        CategoryEntity savedEntity = CategoryEntity.builder()
                .id(1L).name("Electronics").description("Old Desc").active(false).build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(category)).thenReturn(savedEntity);

        CategoryResponse response = categoryService.updateCategory(1L, request);

        assertFalse(response.isActive());
        verify(categoryRepository).save(category);
    }
}
