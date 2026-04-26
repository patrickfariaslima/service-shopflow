package com.shopflow.shopflow.service;

import java.util.List;

import com.shopflow.shopflow.dto.ProductResponse;

public interface ProductService {
    List<ProductResponse> findAll();
}
