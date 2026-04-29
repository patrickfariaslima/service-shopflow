package com.shopflow.shopflow.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "carts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "cart") 
    private List<CartItemEntity> items;

    @OneToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
