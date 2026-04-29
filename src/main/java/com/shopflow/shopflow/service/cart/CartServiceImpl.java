package com.shopflow.shopflow.service.cart;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopflow.shopflow.dto.cart.AddCartItemRequest;
import com.shopflow.shopflow.dto.cart.CartItemResponse;
import com.shopflow.shopflow.dto.cart.CartResponse;
import com.shopflow.shopflow.dto.cart.UpdateCartItemRequest;
import com.shopflow.shopflow.entity.CartEntity;
import com.shopflow.shopflow.entity.CartItemEntity;
import com.shopflow.shopflow.entity.ProductEntity;
import com.shopflow.shopflow.entity.UserEntity;
import com.shopflow.shopflow.exception.BusinessException;
import com.shopflow.shopflow.exception.ResourceNotFoundException;
import com.shopflow.shopflow.repository.CartItemRepository;
import com.shopflow.shopflow.repository.CartRepository;
import com.shopflow.shopflow.repository.ProductRepository;
import com.shopflow.shopflow.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final CartRepository repository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private String notFoundMessage = "Cart not found for user";

    private UserEntity getAuthenticatedUser() {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    return userRepository.findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    @Transactional
    public CartResponse getCart() {
        UserEntity user = getAuthenticatedUser();
        Optional<CartEntity> cart = repository.findByUser(user);

        return cart.map(this::toCartResponse)
                    .orElse(CartResponse.builder().id(null).items(List.of()).total(BigDecimal.ZERO).build());


    }

    private CartResponse toCartResponse(CartEntity cart) {
        List<CartItemResponse> items = cartItemRepository.findByCart(cart).stream()
                .map(item -> CartItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .unitPrice(item.getUnitPrice())
                        .quantity(item.getQuantity())
                        .subtotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .build())
                .toList();
        
        BigDecimal total = items.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return CartResponse.builder()
                            .id(cart.getId())
                            .items(items)
                            .total(total)
                            .build();
    }

    @Override
    @Transactional
    public CartResponse addItem(AddCartItemRequest request) {
        UserEntity user = getAuthenticatedUser();

        ProductEntity product = productRepository.findByIdAndActiveTrue(request.getProductId()).orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));

        if (product.getStockQty() < request.getQuantity() ) {
            throw new BusinessException("Insufficient stock");
        }

        CartEntity cart = repository.findByUser(user)
                .orElseGet(() -> {
                    CartEntity newCart = new CartEntity();
                    newCart.setUser(user);
                    newCart.setCreatedAt(LocalDateTime.now());
                    newCart.setItems(new ArrayList<>());
                    return repository.save(newCart);
                });

        CartItemEntity cartItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(request.getProductId()))
                .findFirst()
                .orElseGet(() -> {
                    CartItemEntity newItem = new CartItemEntity();
                    newItem.setCart(cart);
                    newItem.setQuantity(0);
                    newItem.setProduct(product);
                    newItem.setUnitPrice(product.getPrice());
                    return newItem;
                });

        cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());

        cartItemRepository.save(cartItem);

        return toCartResponse(repository.findByUser(user).orElseThrow(() -> new ResourceNotFoundException(notFoundMessage)));

    }

    @Override
    @Transactional
    public void removeItem(Long productId) {
        UserEntity user = getAuthenticatedUser();

        CartEntity cart = repository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException(notFoundMessage));

        CartItemEntity item = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Item not found in cart"));

        cartItemRepository.delete(item);
    }

    @Override
    @Transactional
    public CartResponse updateItem(Long productId, UpdateCartItemRequest request) {
        UserEntity user = getAuthenticatedUser();

        CartEntity cart = repository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException(notFoundMessage));

        CartItemEntity cartItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Item not found in cart: " + productId));
        
        if (cartItem.getProduct().getStockQty() < request.getQuantity()) {
            throw new BusinessException("Insufficient stock");
        }

        cartItem.setQuantity(request.getQuantity());

        cartItemRepository.save(cartItem);

        return toCartResponse(repository.findByUser(user).orElseThrow(() -> new ResourceNotFoundException(notFoundMessage)));
    }

    @Override
    @Transactional
    public void clearCart() {
        UserEntity user = getAuthenticatedUser();

        CartEntity cart = repository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException(notFoundMessage));

        cartItemRepository.deleteAll(cart.getItems());
    }
}
