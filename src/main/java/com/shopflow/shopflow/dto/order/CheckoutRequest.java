package com.shopflow.shopflow.dto.order;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CheckoutRequest {
    @NotBlank(message = "Address cannot be blank")
    private String address;
}
