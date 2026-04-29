package com.shopflow.shopflow.dto.cart;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartResponse {
    private Long id;
    private List<CartItemResponse> items;
    private BigDecimal total;
}
