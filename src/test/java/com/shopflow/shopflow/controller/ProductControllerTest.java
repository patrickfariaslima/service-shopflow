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

import java.math.BigDecimal;
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
import com.shopflow.shopflow.dto.product.CreateProductRequest;
import com.shopflow.shopflow.dto.product.ProductResponse;
import com.shopflow.shopflow.dto.product.UpdateProductRequest;
import com.shopflow.shopflow.service.auth.DenyListService;
import com.shopflow.shopflow.service.jwt.JwtService;
import com.shopflow.shopflow.service.product.ProductService;
import org.springframework.security.core.userdetails.UserDetailsService;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private DenyListService denyListService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    void findAll_ShouldReturnPageOfProducts() throws Exception {
        ProductResponse product = ProductResponse.builder()
                .id(1L)
                .name("Notebook")
                .price(BigDecimal.valueOf(3000))
                .stockQty(10)
                .build();

        Page<ProductResponse> page = new PageImpl<>(List.of(product));
        when(productService.findAll(any(Pageable.class), any(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/products")
                .param("page", "0")
                .param("size", "12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Notebook"));

        verify(productService).findAll(any(Pageable.class), any(), any(), any(), any());
    }

    @Test
    void findById_WithExistingId_ShouldReturnProduct() throws Exception {
        ProductResponse product = ProductResponse.builder()
                .id(1L)
                .name("Notebook")
                .price(BigDecimal.valueOf(3000))
                .stockQty(10)
                .build();

        when(productService.findById(1L)).thenReturn(product);

        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Notebook"));

        verify(productService).findById(1L);
    }

    @Test
    void createProduct_WithValidRequest_ShouldReturnCreatedProduct() throws Exception {
        CreateProductRequest request = new CreateProductRequest("Notebook", "High-end laptop", BigDecimal.valueOf(3000), 10, 5, null, 1L);

        ProductResponse product = ProductResponse.builder()
                .id(1L)
                .name("Notebook")
                .price(BigDecimal.valueOf(3000))
                .stockQty(10)
                .build();

        when(productService.createProduct(any(CreateProductRequest.class))).thenReturn(product);

        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Notebook"));

        verify(productService).createProduct(any(CreateProductRequest.class));
    }

    @Test
    void updateProduct_WithValidRequest_ShouldReturnUpdatedProduct() throws Exception {
        UpdateProductRequest request = new UpdateProductRequest("Notebook Updated", "Updated description", BigDecimal.valueOf(3500), null, null, null, null, true);

        ProductResponse product = ProductResponse.builder()
                .id(1L)
                .name("Notebook Updated")
                .price(BigDecimal.valueOf(3500))
                .stockQty(10)
                .build();

        when(productService.updateProduct(anyLong(), any(UpdateProductRequest.class))).thenReturn(product);

        mockMvc.perform(put("/api/v1/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Notebook Updated"));

        verify(productService).updateProduct(anyLong(), any(UpdateProductRequest.class));
    }

    @Test
    void deleteProduct_WithExistingId_ShouldReturnNoContent() throws Exception {
        doNothing().when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/api/v1/products/1"))
                .andExpect(status().isNoContent());

        verify(productService).deleteProduct(1L);
    }
}
