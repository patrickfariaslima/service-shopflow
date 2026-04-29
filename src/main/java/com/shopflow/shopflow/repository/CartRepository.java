package com.shopflow.shopflow.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopflow.shopflow.entity.CartEntity;
import com.shopflow.shopflow.entity.UserEntity;

public interface CartRepository extends JpaRepository<CartEntity, Long> {

    Optional<CartEntity> findByUser(UserEntity user);    
}
