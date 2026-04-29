package com.shopflow.shopflow.service.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.shopflow.shopflow.dto.product.CreateProductRequest;
import com.shopflow.shopflow.dto.product.ProductResponse;
import com.shopflow.shopflow.dto.product.UpdateProductRequest;

public interface ProductService {
    Page<ProductResponse> findAll(Pageable pageble);

    ProductResponse findById(Long id);

    ProductResponse createProduct(CreateProductRequest product);

    ProductResponse updateProduct(Long id, UpdateProductRequest product);

    void deleteProduct(Long id);
}
