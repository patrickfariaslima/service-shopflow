package com.shopflow.shopflow.service.cart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.shopflow.shopflow.dto.cart.AddCartItemRequest;
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

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CartServiceImplTest {

    @Mock private CartRepository repository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProductRepository productRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private UserEntity user;

    @BeforeEach
    void setupSecurityContext() {
        user = UserEntity.builder().id(1L).email("patrick@email.com").build();

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("patrick@email.com");
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmail("patrick@email.com")).thenReturn(Optional.of(user));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCart_WhenCartExists_ShouldReturnCartResponse() {
        CartEntity cart = CartEntity.builder().id(1L).user(user).build();

        when(repository.findByUser(user)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCart(cart)).thenReturn(List.of());

        CartResponse response = cartService.getCart();

        assertTrue(response.getItems().isEmpty());
    }

    @Test
    void getCart_WhenNoCartExists_ShouldReturnEmptyResponse() {
        when(repository.findByUser(user)).thenReturn(Optional.empty());

        CartResponse response = cartService.getCart();

        assertTrue(response.getItems().isEmpty());
        assertTrue(BigDecimal.ZERO.compareTo(response.getTotal()) == 0);
    }

    @Test
    void addItem_WhenProductNotFound_ShouldThrowResourceNotFoundException() {
        AddCartItemRequest request = new AddCartItemRequest(99L, 1);

        when(productRepository.findByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cartService.addItem(request));
    }

    @Test
    void addItem_WhenStockIsInsufficient_ShouldThrowBusinessException() {
        ProductEntity product = ProductEntity.builder()
                .id(1L).name("Notebook").stockQty(1).price(BigDecimal.valueOf(3000)).build();
        AddCartItemRequest request = new AddCartItemRequest(1L, 5);

        when(productRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(product));

        assertThrows(BusinessException.class, () -> cartService.addItem(request));
    }

    @Test
    void removeItem_WhenItemNotInCart_ShouldThrowResourceNotFoundException() {
        CartEntity cart = CartEntity.builder().id(1L).user(user).items(new ArrayList<>()).build();

        when(repository.findByUser(user)).thenReturn(Optional.of(cart));

        assertThrows(ResourceNotFoundException.class, () -> cartService.removeItem(99L));
    }

    @Test
    void removeItem_WhenCartNotFound_ShouldThrowResourceNotFoundException() {
        when(repository.findByUser(user)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cartService.removeItem(1L));
    }

    @Test
    void updateItem_WhenStockInsufficient_ShouldThrowBusinessException() {
        ProductEntity product = ProductEntity.builder().id(1L).stockQty(2).price(BigDecimal.valueOf(100)).build();
        CartItemEntity item = CartItemEntity.builder().product(product).quantity(1).unitPrice(BigDecimal.valueOf(100)).build();
        CartEntity cart = CartEntity.builder().id(1L).user(user).items(List.of(item)).build();
        UpdateCartItemRequest request = new UpdateCartItemRequest(10);

        when(repository.findByUser(user)).thenReturn(Optional.of(cart));

        assertThrows(BusinessException.class, () -> cartService.updateItem(1L, request));
    }

    @Test
    void clearCart_WhenCartNotFound_ShouldThrowResourceNotFoundException() {
        when(repository.findByUser(user)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cartService.clearCart());
    }

    @Test
    void clearCart_WhenCartExists_ShouldDeleteAllItems() {
        CartItemEntity item = CartItemEntity.builder().id(1L).build();
        CartEntity cart = CartEntity.builder().id(1L).user(user).items(List.of(item)).build();

        when(repository.findByUser(user)).thenReturn(Optional.of(cart));

        cartService.clearCart();

        verify(cartItemRepository).deleteAll(cart.getItems());
    }

    @Test
    void addItem_WhenCartExistsAndItemNotInCart_ShouldAddItemAndReturnCart() {
        ProductEntity product = ProductEntity.builder()
                .id(1L).name("Notebook").stockQty(10).price(BigDecimal.valueOf(3000)).build();
        CartEntity cart = CartEntity.builder().id(1L).user(user).items(new ArrayList<>()).build();
        AddCartItemRequest request = new AddCartItemRequest(1L, 2);

        CartItemEntity savedItem = CartItemEntity.builder()
                .id(1L).product(product).quantity(2).unitPrice(BigDecimal.valueOf(3000)).build();

        when(productRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(product));
        when(repository.findByUser(user)).thenReturn(Optional.of(cart));
        when(cartItemRepository.save(any(CartItemEntity.class))).thenReturn(savedItem);
        when(cartItemRepository.findByCart(cart)).thenReturn(List.of(savedItem));

        CartResponse response = cartService.addItem(request);

        assertNotNull(response);
        assertEquals(1, response.getItems().size());
        verify(cartItemRepository).save(any(CartItemEntity.class));
    }

    @Test
    void addItem_WhenCartDoesNotExist_ShouldCreateCartAndAddItem() {
        ProductEntity product = ProductEntity.builder()
                .id(1L).name("Notebook").stockQty(10).price(BigDecimal.valueOf(3000)).build();
        CartEntity newCart = CartEntity.builder().id(2L).user(user).items(new ArrayList<>()).build();
        AddCartItemRequest request = new AddCartItemRequest(1L, 1);

        CartItemEntity savedItem = CartItemEntity.builder()
                .id(1L).product(product).quantity(1).unitPrice(BigDecimal.valueOf(3000)).build();

        when(productRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(product));
        when(repository.findByUser(user)).thenReturn(Optional.empty(), Optional.of(newCart));
        when(repository.save(any(CartEntity.class))).thenReturn(newCart);
        when(cartItemRepository.save(any(CartItemEntity.class))).thenReturn(savedItem);
        when(cartItemRepository.findByCart(newCart)).thenReturn(List.of(savedItem));

        CartResponse response = cartService.addItem(request);

        assertNotNull(response);
        verify(repository).save(any(CartEntity.class));
    }

    @Test
    void addItem_WhenItemAlreadyInCart_ShouldUpdateQuantityAndReturnCart() {
        ProductEntity product = ProductEntity.builder()
                .id(1L).name("Notebook").stockQty(10).price(BigDecimal.valueOf(3000)).build();
        CartItemEntity existingItem = CartItemEntity.builder()
                .id(1L).product(product).quantity(2).unitPrice(BigDecimal.valueOf(3000)).build();
        CartEntity cart = CartEntity.builder().id(1L).user(user).items(new ArrayList<>(List.of(existingItem))).build();
        AddCartItemRequest request = new AddCartItemRequest(1L, 3);

        CartItemEntity updatedItem = CartItemEntity.builder()
                .id(1L).product(product).quantity(5).unitPrice(BigDecimal.valueOf(3000)).build();

        when(productRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(product));
        when(repository.findByUser(user)).thenReturn(Optional.of(cart));
        when(cartItemRepository.save(existingItem)).thenReturn(updatedItem);
        when(cartItemRepository.findByCart(cart)).thenReturn(List.of(updatedItem));

        CartResponse response = cartService.addItem(request);

        assertNotNull(response);
        assertEquals(5, existingItem.getQuantity());
    }

    @Test
    void removeItem_WhenItemExists_ShouldDeleteItem() {
        ProductEntity product = ProductEntity.builder().id(1L).build();
        CartItemEntity item = CartItemEntity.builder().id(1L).product(product).build();
        CartEntity cart = CartEntity.builder().id(1L).user(user).items(new ArrayList<>(List.of(item))).build();

        when(repository.findByUser(user)).thenReturn(Optional.of(cart));

        cartService.removeItem(1L);

        verify(cartItemRepository).delete(item);
    }

    @Test
    void updateItem_WhenItemExistsAndStockSufficient_ShouldUpdateAndReturnCart() {
        ProductEntity product = ProductEntity.builder().id(1L).stockQty(20).price(BigDecimal.valueOf(100)).build();
        CartItemEntity item = CartItemEntity.builder()
                .id(1L).product(product).quantity(2).unitPrice(BigDecimal.valueOf(100)).build();
        CartEntity cart = CartEntity.builder().id(1L).user(user).items(new ArrayList<>(List.of(item))).build();
        UpdateCartItemRequest request = new UpdateCartItemRequest(5);

        CartItemEntity updatedItem = CartItemEntity.builder()
                .id(1L).product(product).quantity(5).unitPrice(BigDecimal.valueOf(100)).build();

        when(repository.findByUser(user)).thenReturn(Optional.of(cart));
        when(cartItemRepository.save(item)).thenReturn(updatedItem);
        when(cartItemRepository.findByCart(cart)).thenReturn(List.of(updatedItem));

        CartResponse response = cartService.updateItem(1L, request);

        assertNotNull(response);
        assertEquals(5, item.getQuantity());
    }

    @Test
    void updateItem_WhenItemNotInCart_ShouldThrowResourceNotFoundException() {
        CartEntity cart = CartEntity.builder().id(1L).user(user).items(new ArrayList<>()).build();
        UpdateCartItemRequest request = new UpdateCartItemRequest(5);

        when(repository.findByUser(user)).thenReturn(Optional.of(cart));

        assertThrows(ResourceNotFoundException.class, () -> cartService.updateItem(99L, request));
    }

    @Test
    void updateItem_WhenCartNotFound_ShouldThrowResourceNotFoundException() {
        UpdateCartItemRequest request = new UpdateCartItemRequest(5);

        when(repository.findByUser(user)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cartService.updateItem(1L, request));
    }
}
