package com.shopflow.shopflow.dto.cart;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCartItemRequest {
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be a positive integer")
    private Integer quantity;
}
