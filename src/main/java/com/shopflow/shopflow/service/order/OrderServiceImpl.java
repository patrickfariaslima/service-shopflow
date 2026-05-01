package com.shopflow.shopflow.service.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopflow.shopflow.dto.order.CheckoutRequest;
import com.shopflow.shopflow.dto.order.OrderItemResponse;
import com.shopflow.shopflow.dto.order.OrderResponse;
import com.shopflow.shopflow.dto.order.UpdateOrderStatusRequest;
import com.shopflow.shopflow.entity.CartEntity;
import com.shopflow.shopflow.entity.CartItemEntity;
import com.shopflow.shopflow.entity.OrderEntity;
import com.shopflow.shopflow.entity.OrderItemEntity;
import com.shopflow.shopflow.entity.ProductEntity;
import com.shopflow.shopflow.entity.StockMovementEntity;
import com.shopflow.shopflow.entity.UserEntity;
import com.shopflow.shopflow.enums.MovementType;
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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final StockMovementRepository stockMovementRepository;

    private String cartEmptyText = "Cart is empty.";
    private String orderNotFoundText = "Order not found.";    

    private UserEntity getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found")) ;
    }

    private OrderResponse toOrderResponse(OrderEntity order) {
        List<OrderItemEntity> items = orderItemRepository.findByOrder(order);
        List<OrderItemResponse> itemResponses = items.stream()
                .map(item -> OrderItemResponse.builder()
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .build()
                        
                ).toList();
        BigDecimal total = itemResponses.stream()
                .map(OrderItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
        
        return OrderResponse.builder()
                .id(order.getId())
                .status(order.getStatus())
                .totalAmount(total)
                .address(order.getAddress())
                .createdAt(order.getCreatedAt())
                .items(itemResponses)
                .build();
    }


    @Override
    @Transactional
    public OrderResponse checkout(CheckoutRequest request) {
        UserEntity user = getAuthenticatedUser();
        CartEntity cart = cartRepository.findByUser(user).orElseThrow(() -> new ResourceNotFoundException(cartEmptyText));

        List<CartItemEntity> cartItems = cartItemRepository.findByCart(cart);

        if (cartItems.isEmpty()) {
            throw new ResourceNotFoundException(cartEmptyText);
        }

        for (CartItemEntity item : cartItems) {
            if(item.getProduct().getStockQty() < item.getQuantity()) {
                throw new BusinessException("Insufficient stock for: " + item.getProduct().getName());
            } 
        }

        BigDecimal totalAmount = cartItems.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add );
        


        OrderEntity order = OrderEntity.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .totalAmount(totalAmount)
                .address(request.getAddress())
                .createdAt(LocalDateTime.now())
                .build();

        OrderEntity savedOrder = orderRepository.save(order);

        for (CartItemEntity cartItem : cartItems) {
            OrderItemEntity orderItem = OrderItemEntity.builder()
                    .order(savedOrder)
                    .product(cartItem.getProduct())
                    .quantity(cartItem.getQuantity())
                    .unitPrice(cartItem.getUnitPrice())
                    .build();

            orderItemRepository.save(orderItem);

            ProductEntity product = cartItem.getProduct();

            product.setStockQty(product.getStockQty() - cartItem.getQuantity());

            productRepository.save(product);

            StockMovementEntity stockMovement = StockMovementEntity.builder()
                    .product(product)
                    .type(MovementType.OUT)
                    .quantity(cartItem.getQuantity())
                    .reason("Order #" + savedOrder.getId())
                    .order(savedOrder)
                    .createdAt(LocalDateTime.now())
                    .build();
            
            stockMovementRepository.save(stockMovement);
        }

        cartItemRepository.deleteAll(cartItems);
        return toOrderResponse(savedOrder);
    }

    @Override
    public List<OrderResponse> getOrderHistory() {
        UserEntity user = getAuthenticatedUser();
        List<OrderEntity> orders = orderRepository.findByUser(user);

        return orders.stream()
                .map(this::toOrderResponse)
                .toList();
    }

    @Override
    public OrderResponse getOrderById(Long id) {
        OrderEntity order = orderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(orderNotFoundText));

        UserEntity user = getAuthenticatedUser();

        if(!order.getUser().getId().equals(user.getId())) {
            throw new BusinessException("You don't have permission to access this order");
        }

        return toOrderResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse updateStatus(Long id, UpdateOrderStatusRequest request) {
        OrderEntity order = orderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(orderNotFoundText));

        if (request.getStatus() == OrderStatus.CANCELLED && order.getStatus() != OrderStatus.CANCELLED) {
            List<OrderItemEntity> items = orderItemRepository.findByOrder(order);

            for (OrderItemEntity item : items) {
                ProductEntity product = item.getProduct();

                product.setStockQty(product.getStockQty() + item.getQuantity());

                productRepository.save(product);

                StockMovementEntity stockMovement = StockMovementEntity.builder()
                        .product(product)
                        .type(MovementType.IN)
                        .reason("Cancellation of order: " + order.getId())
                        .order(order)
                        .quantity(item.getQuantity())
                        .createdAt(LocalDateTime.now())
                        .build();

                stockMovementRepository.save(stockMovement);
            }
        }

        order.setStatus(request.getStatus());
        orderRepository.save(order);

        return toOrderResponse(order);
    }

    @Override
    public List<OrderResponse> getAllOrders() {
        List<OrderEntity> orders = orderRepository.findAll();

        return orders.stream()
                .map(this::toOrderResponse)
                .toList();
    }

}
