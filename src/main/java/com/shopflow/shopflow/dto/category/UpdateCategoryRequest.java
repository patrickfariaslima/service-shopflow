package com.shopflow.shopflow.dto.category;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCategoryRequest {
    @Size(max = 80, message = "Name cannot exceed 80 characters")
    private String name;
    private String description;
    private Boolean active;
}

