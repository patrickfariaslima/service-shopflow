package com.shopflow.shopflow.dto.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.shopflow.shopflow.enums.OrderStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {
    private Long id;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String address;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;
}
