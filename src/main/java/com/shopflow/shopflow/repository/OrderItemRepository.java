package com.shopflow.shopflow.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopflow.shopflow.entity.OrderEntity;
import com.shopflow.shopflow.entity.OrderItemEntity;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Long> {
    List<OrderItemEntity> findByOrder(OrderEntity order);
}
