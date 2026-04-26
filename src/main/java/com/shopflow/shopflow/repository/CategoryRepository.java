package com.shopflow.shopflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopflow.shopflow.entity.CategoryEntity;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
    
}
