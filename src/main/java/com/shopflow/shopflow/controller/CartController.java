package com.shopflow.shopflow.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopflow.shopflow.dto.cart.AddCartItemRequest;
import com.shopflow.shopflow.dto.cart.CartResponse;
import com.shopflow.shopflow.dto.cart.UpdateCartItemRequest;
import com.shopflow.shopflow.service.cart.CartService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<CartResponse> getCart() {
        return ResponseEntity.ok(cartService.getCart());
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(@Valid @RequestBody AddCartItemRequest request) {

        CartResponse addItem = cartService.addItem(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(addItem);
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/items/{productId}")
    public ResponseEntity<CartResponse> updateItem(@PathVariable Long productId, @Valid @RequestBody UpdateCartItemRequest request) {
        CartResponse updatedItem = cartService.updateItem(productId, request);
        
        return ResponseEntity.ok(updatedItem);
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/items/{productId}")
    public ResponseEntity<Void> removeItem(@PathVariable Long productId) {
        cartService.removeItem(productId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping
    public ResponseEntity<Void> clearCart() {
        cartService.clearCart();
        return ResponseEntity.noContent().build();
    }
}
