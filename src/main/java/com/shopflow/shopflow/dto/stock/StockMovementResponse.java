package com.shopflow.shopflow.dto.stock;

import java.time.LocalDateTime;

import com.shopflow.shopflow.enums.MovementType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockMovementResponse {
    private Long id;
    private MovementType type; 
    private Integer quantity;
    private String reason;
    private Long orderId;
    private LocalDateTime createdAt; 
}
