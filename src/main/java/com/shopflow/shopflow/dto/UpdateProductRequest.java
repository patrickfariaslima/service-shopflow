package com.shopflow.shopflow.dto;

import java.math.BigDecimal;


import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductRequest {
    @Size(max = 150, message = "Name cannot exceed 150 characters")
    private String name;

    private String description;

    @Positive(message = "Price must be a positive number")
    private BigDecimal price;

    @PositiveOrZero(message = "Stock quantity cannot be negative")
    private Integer stockQty;

    @PositiveOrZero(message = "Stock threshold cannot be negative")
    private Integer stockThreshold;

    @Size(max = 300, message = "Image URL cannot exceed 300 characters")
    private String imageUrl;
    
    private Long categoryId;
}
