package com.shopflow.shopflow.service;

import java.util.List;

import com.shopflow.shopflow.dto.CreateProductRequest;
import com.shopflow.shopflow.dto.ProductResponse;
import com.shopflow.shopflow.dto.UpdateProductRequest;

public interface ProductService {
    List<ProductResponse> findAll();

    ProductResponse findById(Long id);

    ProductResponse createProduct(CreateProductRequest product);

    ProductResponse updateProduct(Long id, UpdateProductRequest product);

    void deleteProduct(Long id);
}
