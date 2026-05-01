package com.shopflow.shopflow.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.shopflow.shopflow.entity.OrderEntity;
import com.shopflow.shopflow.entity.UserEntity;

public interface OrderRepository extends JpaRepository<OrderEntity, Long>{

    List<OrderEntity> findByUser(UserEntity user);
    
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM OrderEntity o " +
           "WHERE CAST(o.createdAt AS date) = CURRENT_DATE " +
            "AND o.status <> 'CANCELLED'")
    BigDecimal findTodayRevenue();
    
}
