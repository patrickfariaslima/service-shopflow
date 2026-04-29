package com.shopflow.shopflow.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopflow.shopflow.entity.CartEntity;
import com.shopflow.shopflow.entity.CartItemEntity;

public interface CartItemRepository extends JpaRepository<CartItemEntity, Long>{

    List<CartItemEntity> findByCart(CartEntity cart);
    
}
