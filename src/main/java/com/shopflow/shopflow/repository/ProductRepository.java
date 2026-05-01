package com.shopflow.shopflow.repository;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.shopflow.shopflow.entity.ProductEntity;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
    Page<ProductEntity> findByActiveTrue(Pageable pageable);

    Optional<ProductEntity> findByIdAndActiveTrue(Long id);

    @Query("SELECT p FROM  ProductEntity p WHERE p.active = true " +
        "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
        "AND (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
        "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
        "AND (:maxPrice IS NULL OR p.price <= :maxPrice)")
    Page<ProductEntity> findWithFilters (
        @Param("categoryId") Long categoryId,
        @Param("name") String name,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        Pageable pageable
    );
}
