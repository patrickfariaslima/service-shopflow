package com.shopflow.shopflow.service.cart;

import com.shopflow.shopflow.dto.cart.AddCartItemRequest;

import com.shopflow.shopflow.dto.cart.CartResponse;
import com.shopflow.shopflow.dto.cart.UpdateCartItemRequest;

public interface CartService {

    CartResponse getCart();

    CartResponse addItem(AddCartItemRequest request);

    CartResponse updateItem(Long productId, UpdateCartItemRequest request);

    void removeItem(Long productId);

    void clearCart();
}
