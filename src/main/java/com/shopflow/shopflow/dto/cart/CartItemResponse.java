package com.shopflow.shopflow.dto.cart;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal subtotal;
}
