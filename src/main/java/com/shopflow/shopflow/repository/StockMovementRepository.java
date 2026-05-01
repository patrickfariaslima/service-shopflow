package com.shopflow.shopflow.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopflow.shopflow.entity.ProductEntity;
import com.shopflow.shopflow.entity.StockMovementEntity;

public interface StockMovementRepository extends JpaRepository<StockMovementEntity, Long> {
    List<StockMovementEntity> findByProduct(ProductEntity product);

    List<StockMovementEntity> findByProductOrderByCreatedAtDesc(ProductEntity product);
}
