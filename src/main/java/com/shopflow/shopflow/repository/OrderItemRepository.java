package com.shopflow.shopflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopflow.shopflow.entity.OrderItemEntity;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Long> {
    
}
