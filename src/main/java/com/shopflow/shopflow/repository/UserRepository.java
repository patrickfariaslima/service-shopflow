package com.shopflow.shopflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopflow.shopflow.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    
}
