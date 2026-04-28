package com.shopflow.shopflow.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.shopflow.shopflow.entity.CategoryEntity;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
    Page<CategoryEntity> findByActiveTrue(Pageable pageable);

    Optional<CategoryEntity> findByIdAndActiveTrue(Long id);
}
