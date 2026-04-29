package com.shopflow.shopflow.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopflow.shopflow.entity.OrderEntity;
import com.shopflow.shopflow.entity.UserEntity;

public interface OrderRepository extends JpaRepository<OrderEntity, Long>{

    List<OrderEntity> findByUser(UserEntity user);
    
}
