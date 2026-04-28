package com.shopflow.shopflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreateCategoryRequest {
    @NotBlank(message = "Name cannot be blank")
    @Size(max = 80, message = "Name cannot exceed 80 characters")
    private String name;
    private String description;
}
