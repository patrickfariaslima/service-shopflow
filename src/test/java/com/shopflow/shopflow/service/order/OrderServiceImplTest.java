package com.shopflow.shopflow.service.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.shopflow.shopflow.dto.order.CheckoutRequest;
import com.shopflow.shopflow.dto.order.OrderResponse;
import com.shopflow.shopflow.dto.order.UpdateOrderStatusRequest;
import com.shopflow.shopflow.entity.CartEntity;
import com.shopflow.shopflow.entity.CartItemEntity;
import com.shopflow.shopflow.entity.OrderEntity;
import com.shopflow.shopflow.entity.OrderItemEntity;
import com.shopflow.shopflow.entity.ProductEntity;
import com.shopflow.shopflow.entity.UserEntity;
import com.shopflow.shopflow.enums.OrderStatus;
import com.shopflow.shopflow.exception.BusinessException;
import com.shopflow.shopflow.exception.ResourceNotFoundException;
import com.shopflow.shopflow.repository.CartItemRepository;
import com.shopflow.shopflow.repository.CartRepository;
import com.shopflow.shopflow.repository.OrderItemRepository;
import com.shopflow.shopflow.repository.OrderRepository;
import com.shopflow.shopflow.repository.ProductRepository;
import com.shopflow.shopflow.repository.StockMovementRepository;
import com.shopflow.shopflow.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;
    @Mock private StockMovementRepository stockMovementRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

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
    void checkout_WhenStockIsInsufficient_ShouldThrowBusinessException() {
        CartEntity cart = CartEntity.builder().id(1L).user(user).build();
        ProductEntity product = ProductEntity.builder()
                .id(1L).name("Notebook").stockQty(1).price(BigDecimal.valueOf(3000)).build();
        CartItemEntity cartItem = CartItemEntity.builder()
                .cart(cart).product(product).quantity(5).unitPrice(BigDecimal.valueOf(3000)).build();

        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCart(cart)).thenReturn(List.of(cartItem));

        CheckoutRequest insufficientRequest = new CheckoutRequest("Rua A, 123");
        assertThrows(BusinessException.class, () -> orderService.checkout(insufficientRequest));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void checkout_WhenCartIsEmpty_ShouldThrowResourceNotFoundException() {
        CartEntity cart = CartEntity.builder().id(1L).user(user).build();

        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCart(cart)).thenReturn(List.of());

        CheckoutRequest emptyCartRequest = new CheckoutRequest("Rua A, 123");
        assertThrows(ResourceNotFoundException.class, () -> orderService.checkout(emptyCartRequest));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void getOrderById_WhenUserIsNotOwner_ShouldThrowBusinessException() {
        UserEntity otherUser = UserEntity.builder().id(2L).email("outro@email.com").build();
        OrderEntity order = OrderEntity.builder()
                .id(1L).user(otherUser).status(OrderStatus.PENDING).build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(BusinessException.class, () -> orderService.getOrderById(1L));
    }

    @Test
    void getOrderById_WhenOrderNotFound_ShouldThrowResourceNotFoundException() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrderById(99L));
    }

    @Test
    void updateStatus_WhenCancelledOrder_ShouldRestoreStock() {
        ProductEntity product = ProductEntity.builder()
                .id(1L).name("Notebook").stockQty(5).price(BigDecimal.valueOf(3000)).build();
        OrderEntity order = OrderEntity.builder()
                .id(1L).user(user).status(OrderStatus.PAID).createdAt(LocalDateTime.now()).build();
        OrderItemEntity item = OrderItemEntity.builder()
                .order(order).product(product).quantity(2).unitPrice(BigDecimal.valueOf(3000)).build();
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(OrderStatus.CANCELLED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrder(order)).thenReturn(List.of(item));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderItemRepository.findByOrder(order)).thenReturn(List.of(item));

        orderService.updateStatus(1L, request);

        assertEquals(7, product.getStockQty());
        verify(productRepository).save(product);
        verify(stockMovementRepository).save(any());
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    void updateStatus_WhenOrderNotFound_ShouldThrowResourceNotFoundException() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        UpdateOrderStatusRequest paidRequest = new UpdateOrderStatusRequest(OrderStatus.PAID);
        assertThrows(ResourceNotFoundException.class, () -> orderService.updateStatus(99L, paidRequest));
    }

    @Test
    void checkout_WhenCartNotFound_ShouldThrowResourceNotFoundException() {
        when(cartRepository.findByUser(user)).thenReturn(Optional.empty());

        CheckoutRequest request = new CheckoutRequest("Rua A, 123");
        assertThrows(ResourceNotFoundException.class, () -> orderService.checkout(request));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void checkout_WhenStockSufficient_ShouldCreateOrderAndReduceStock() {
        ProductEntity product = ProductEntity.builder()
                .id(1L).name("Notebook").stockQty(10).price(BigDecimal.valueOf(3000)).build();
        CartEntity cart = CartEntity.builder().id(1L).user(user).build();
        CartItemEntity cartItem = CartItemEntity.builder()
                .cart(cart).product(product).quantity(2).unitPrice(BigDecimal.valueOf(3000)).build();
        OrderEntity savedOrder = OrderEntity.builder()
                .id(1L).user(user).status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(6000)).address("Rua A, 123")
                .createdAt(LocalDateTime.now()).build();

        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCart(cart)).thenReturn(List.of(cartItem));
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(savedOrder);
        when(orderItemRepository.findByOrder(savedOrder)).thenReturn(List.of());

        CheckoutRequest request = new CheckoutRequest("Rua A, 123");
        OrderResponse response = orderService.checkout(request);

        assertNotNull(response);
        assertEquals(8, product.getStockQty());
        verify(orderRepository).save(any(OrderEntity.class));
        verify(productRepository).save(product);
        verify(stockMovementRepository).save(any());
        verify(cartItemRepository).deleteAll(List.of(cartItem));
    }

    @Test
    void getOrderById_WhenUserIsOwner_ShouldReturnOrderResponse() {
        OrderEntity order = OrderEntity.builder()
                .id(1L).user(user).status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(3000)).address("Rua A, 123")
                .createdAt(LocalDateTime.now()).build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrder(order)).thenReturn(List.of());

        OrderResponse response = orderService.getOrderById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(OrderStatus.PENDING, response.getStatus());
    }

    @Test
    void getOrderHistory_ShouldReturnAllUserOrders() {
        OrderEntity order = OrderEntity.builder()
                .id(1L).user(user).status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(3000)).address("Rua A, 123")
                .createdAt(LocalDateTime.now()).build();

        when(orderRepository.findByUser(user)).thenReturn(List.of(order));
        when(orderItemRepository.findByOrder(order)).thenReturn(List.of());

        List<OrderResponse> result = orderService.getOrderHistory();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void getAllOrders_ShouldReturnAllOrders() {
        OrderEntity order = OrderEntity.builder()
                .id(1L).user(user).status(OrderStatus.DELIVERED)
                .totalAmount(BigDecimal.valueOf(5000)).address("Rua B, 456")
                .createdAt(LocalDateTime.now()).build();

        when(orderRepository.findAll()).thenReturn(List.of(order));
        when(orderItemRepository.findByOrder(order)).thenReturn(List.of());

        List<OrderResponse> result = orderService.getAllOrders();

        assertEquals(1, result.size());
        assertEquals(OrderStatus.DELIVERED, result.get(0).getStatus());
    }

    @Test
    void updateStatus_WhenStatusIsNotCancelled_ShouldUpdateWithoutRestoringStock() {
        OrderEntity order = OrderEntity.builder()
                .id(1L).user(user).status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now()).build();
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(OrderStatus.PAID);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderItemRepository.findByOrder(order)).thenReturn(List.of());

        OrderResponse response = orderService.updateStatus(1L, request);

        assertNotNull(response);
        assertEquals(OrderStatus.PAID, order.getStatus());
        verify(productRepository, never()).save(any());
        verify(stockMovementRepository, never()).save(any());
    }

    @Test
    void updateStatus_WhenAlreadyCancelled_ShouldNotRestoreStock() {
        OrderEntity order = OrderEntity.builder()
                .id(1L).user(user).status(OrderStatus.CANCELLED)
                .createdAt(LocalDateTime.now()).build();
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(OrderStatus.CANCELLED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderItemRepository.findByOrder(order)).thenReturn(List.of());

        OrderResponse response = orderService.updateStatus(1L, request);

        assertNotNull(response);
        verify(productRepository, never()).save(any());
    }
}
