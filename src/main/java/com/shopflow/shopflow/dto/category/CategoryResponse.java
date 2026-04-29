package com.shopflow.shopflow.dto.category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponse {
    private Long id;
    private String name;
    private String description;
    private boolean active;
}
