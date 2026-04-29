package com.shopflow.shopflow.dto.order;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemResponse {
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}
