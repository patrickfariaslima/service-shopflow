package com.shopflow.shopflow.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.shopflow.shopflow.dto.CreateProductRequest;
import com.shopflow.shopflow.dto.ProductResponse;
import com.shopflow.shopflow.dto.UpdateProductRequest;

public interface ProductService {
    Page<ProductResponse> findAll(Pageable pageble);

    ProductResponse findById(Long id);

    ProductResponse createProduct(CreateProductRequest product);

    ProductResponse updateProduct(Long id, UpdateProductRequest product);

    void deleteProduct(Long id);
}
