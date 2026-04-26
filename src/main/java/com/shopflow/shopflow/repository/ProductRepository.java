package com.shopflow.shopflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopflow.shopflow.entity.ProductEntity;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
    
}
