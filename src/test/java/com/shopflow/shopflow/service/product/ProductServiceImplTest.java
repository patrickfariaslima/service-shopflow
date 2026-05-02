package com.shopflow.shopflow.service.product;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.shopflow.shopflow.dto.product.CreateProductRequest;
import com.shopflow.shopflow.dto.product.ProductResponse;
import com.shopflow.shopflow.dto.product.UpdateProductRequest;
import com.shopflow.shopflow.entity.CategoryEntity;
import com.shopflow.shopflow.entity.ProductEntity;
import com.shopflow.shopflow.exception.ResourceNotFoundException;
import com.shopflow.shopflow.repository.CategoryRepository;
import com.shopflow.shopflow.repository.ProductRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock private ProductRepository productRepository;
    @Mock private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void findById_WhenProductExists_ShouldReturnProductResponse() {
        ProductEntity product = ProductEntity.builder()
                .id(1L).name("Notebook").price(BigDecimal.valueOf(3000))
                .stockQty(10).active(true).build();

        when(productRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(product));

        ProductResponse response = productService.findById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Notebook", response.getName());
        assertEquals(BigDecimal.valueOf(3000), response.getPrice());
    }

    @Test
    void findById_WhenProductNotFound_ShouldThrowResourceNotFoundException() {
        when(productRepository.findByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.findById(99L));
    }

    @Test
    void createProduct_WithValidData_ShouldReturnProductResponse() {
        CreateProductRequest request = new CreateProductRequest(
                "Notebook", "Descricao", BigDecimal.valueOf(3000), 10, 2, null, null);

        ProductEntity savedEntity = ProductEntity.builder()
                .id(1L).name("Notebook").description("Descricao")
                .price(BigDecimal.valueOf(3000)).stockQty(10).active(true).build();

        when(productRepository.save(any(ProductEntity.class))).thenReturn(savedEntity);

        ProductResponse response = productService.createProduct(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Notebook", response.getName());
        verify(productRepository).save(any(ProductEntity.class));
    }

    @Test
    void createProduct_WhenCategoryNotFound_ShouldThrowResourceNotFoundException() {
        CreateProductRequest request = new CreateProductRequest(
                "Notebook", null, BigDecimal.valueOf(3000), 10, null, null, 99L);

        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.createProduct(request));
        verify(productRepository, never()).save(any());
    }

    @Test
    void deleteProduct_WhenProductExists_ShouldDeactivateProduct() {
        ProductEntity product = ProductEntity.builder().id(1L).name("Notebook").active(true).build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.deleteProduct(1L);

        assertFalse(product.isActive());
        verify(productRepository).save(product);
    }

    @Test
    void deleteProduct_WhenProductNotFound_ShouldThrowResourceNotFoundException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.deleteProduct(99L));
        verify(productRepository, never()).save(any());
    }

    @Test
    void findAll_ShouldReturnMappedPage() {
        CategoryEntity category = CategoryEntity.builder().id(1L).name("Electronics").build();
        ProductEntity product = ProductEntity.builder()
                .id(1L).name("Notebook").price(BigDecimal.valueOf(3000))
                .stockQty(10).active(true).category(category).build();

        Pageable pageable = PageRequest.of(0, 12);
        when(productRepository.findWithFilters(null, null, null, null, pageable))
                .thenReturn(new PageImpl<>(List.of(product)));

        Page<ProductResponse> result = productService.findAll(pageable, null, null, null, null);

        assertEquals(1, result.getTotalElements());
        assertEquals("Notebook", result.getContent().get(0).getName());
        assertEquals("Electronics", result.getContent().get(0).getCategoryName());
    }

    @Test
    void findAll_WhenProductHasNoCategory_ShouldReturnNullCategoryName() {
        ProductEntity product = ProductEntity.builder()
                .id(1L).name("Notebook").price(BigDecimal.valueOf(3000))
                .stockQty(10).active(true).category(null).build();

        Pageable pageable = PageRequest.of(0, 12);
        when(productRepository.findWithFilters(null, null, null, null, pageable))
                .thenReturn(new PageImpl<>(List.of(product)));

        Page<ProductResponse> result = productService.findAll(pageable, null, null, null, null);

        assertNotNull(result.getContent().get(0));
        assertEquals(null, result.getContent().get(0).getCategoryName());
    }

    @Test
    void findById_WhenProductHasCategory_ShouldReturnCategoryName() {
        CategoryEntity category = CategoryEntity.builder().id(1L).name("Electronics").build();
        ProductEntity product = ProductEntity.builder()
                .id(1L).name("Notebook").price(BigDecimal.valueOf(3000))
                .stockQty(10).active(true).category(category).build();

        when(productRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(product));

        ProductResponse response = productService.findById(1L);

        assertEquals("Electronics", response.getCategoryName());
    }

    @Test
    void createProduct_WhenCategoryFound_ShouldReturnProductWithCategory() {
        CategoryEntity category = CategoryEntity.builder().id(1L).name("Electronics").build();
        CreateProductRequest request = new CreateProductRequest(
                "Notebook", "Desc", BigDecimal.valueOf(3000), 10, 2, null, 1L);

        ProductEntity savedEntity = ProductEntity.builder()
                .id(1L).name("Notebook").price(BigDecimal.valueOf(3000))
                .stockQty(10).active(true).category(category).build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(ProductEntity.class))).thenReturn(savedEntity);

        ProductResponse response = productService.createProduct(request);

        assertNotNull(response);
        assertEquals("Electronics", response.getCategoryName());
    }

    @Test
    void updateProduct_WhenProductFound_ShouldUpdateFields() {
        ProductEntity entity = ProductEntity.builder()
                .id(1L).name("Old Name").price(BigDecimal.valueOf(1000)).stockQty(5).active(true).build();
        UpdateProductRequest request = new UpdateProductRequest(
                "New Name", "New Desc", BigDecimal.valueOf(2000), 10, 3, "img.jpg", null, false);

        ProductEntity updatedEntity = ProductEntity.builder()
                .id(1L).name("New Name").description("New Desc")
                .price(BigDecimal.valueOf(2000)).stockQty(10).active(false).build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(productRepository.save(entity)).thenReturn(updatedEntity);

        ProductResponse response = productService.updateProduct(1L, request);

        assertNotNull(response);
        assertEquals("New Name", response.getName());
        verify(productRepository).save(entity);
    }

    @Test
    void updateProduct_WhenProductNotFound_ShouldThrowResourceNotFoundException() {
        UpdateProductRequest request = new UpdateProductRequest("Name", null, null, null, null, null, null, null);
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.updateProduct(99L, request));
    }

    @Test
    void updateProduct_WhenCategoryIdProvided_ShouldUpdateCategory() {
        CategoryEntity category = CategoryEntity.builder().id(1L).name("Electronics").build();
        ProductEntity entity = ProductEntity.builder().id(1L).name("Notebook").active(true).build();
        UpdateProductRequest request = new UpdateProductRequest(null, null, null, null, null, null, 1L, null);

        ProductEntity updatedEntity = ProductEntity.builder()
                .id(1L).name("Notebook").category(category).active(true).build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(entity)).thenReturn(updatedEntity);

        ProductResponse response = productService.updateProduct(1L, request);

        assertEquals("Electronics", response.getCategoryName());
    }

    @Test
    void updateProduct_WhenCategoryNotFound_ShouldThrowResourceNotFoundException() {
        ProductEntity entity = ProductEntity.builder().id(1L).name("Notebook").active(true).build();
        UpdateProductRequest request = new UpdateProductRequest(null, null, null, null, null, null, 99L, null);

        when(productRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.updateProduct(1L, request));
    }
}
