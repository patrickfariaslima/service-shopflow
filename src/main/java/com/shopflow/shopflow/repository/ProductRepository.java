package com.shopflow.shopflow.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.shopflow.shopflow.entity.ProductEntity;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
    Page<ProductEntity> findByActiveTrue(Pageable pageable);

    Optional<ProductEntity> findByIdAndActiveTrue(Long id);
}
