package com.shopflow.shopflow.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopflow.shopflow.dto.category.CategoryResponse;
import com.shopflow.shopflow.dto.category.CreateCategoryRequest;
import com.shopflow.shopflow.dto.category.UpdateCategoryRequest;
import com.shopflow.shopflow.service.auth.DenyListService;
import com.shopflow.shopflow.service.category.CategoryService;
import com.shopflow.shopflow.service.jwt.JwtService;
import org.springframework.security.core.userdetails.UserDetailsService;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private DenyListService denyListService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    void findAll_ShouldReturnPageOfCategories() throws Exception {
        CategoryResponse category = CategoryResponse.builder()
                .id(1L)
                .name("Electronics")
                .description("Electronic products")
                .build();

        Page<CategoryResponse> page = new PageImpl<>(List.of(category));
        when(categoryService.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/products/categories")
                .param("page", "0")
                .param("size", "12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Electronics"));

        verify(categoryService).findAll(any(Pageable.class));
    }

    @Test
    void findById_WithExistingId_ShouldReturnCategory() throws Exception {
        CategoryResponse category = CategoryResponse.builder()
                .id(1L)
                .name("Electronics")
                .description("Electronic products")
                .build();

        when(categoryService.findById(1L)).thenReturn(category);

        mockMvc.perform(get("/api/v1/products/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Electronics"));

        verify(categoryService).findById(1L);
    }

    @Test
    void createCategory_WithValidRequest_ShouldReturnCreatedCategory() throws Exception {
        CreateCategoryRequest request = new CreateCategoryRequest("Electronics", "Electronic products");

        CategoryResponse category = CategoryResponse.builder()
                .id(1L)
                .name("Electronics")
                .description("Electronic products")
                .build();

        when(categoryService.createCategory(any(CreateCategoryRequest.class))).thenReturn(category);

        mockMvc.perform(post("/api/v1/products/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Electronics"));

        verify(categoryService).createCategory(any(CreateCategoryRequest.class));
    }

    @Test
    void updateCategory_WithValidRequest_ShouldReturnUpdatedCategory() throws Exception {
        UpdateCategoryRequest request = new UpdateCategoryRequest("Electronics Updated", "Updated description", true);

        CategoryResponse category = CategoryResponse.builder()
                .id(1L)
                .name("Electronics Updated")
                .description("Updated description")
                .build();

        when(categoryService.updateCategory(anyLong(), any(UpdateCategoryRequest.class))).thenReturn(category);

        mockMvc.perform(put("/api/v1/products/categories/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Electronics Updated"));

        verify(categoryService).updateCategory(anyLong(), any(UpdateCategoryRequest.class));
    }

    @Test
    void deleteCategory_WithExistingId_ShouldReturnNoContent() throws Exception {
        doNothing().when(categoryService).deleteCategory(1L);

        mockMvc.perform(delete("/api/v1/products/categories/1"))
                .andExpect(status().isNoContent());

        verify(categoryService).deleteCategory(1L);
    }
}
