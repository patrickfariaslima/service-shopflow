package com.shopflow.shopflow.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopflow.shopflow.dto.order.CheckoutRequest;
import com.shopflow.shopflow.dto.order.OrderItemResponse;
import com.shopflow.shopflow.dto.order.OrderResponse;
import com.shopflow.shopflow.dto.order.UpdateOrderStatusRequest;
import com.shopflow.shopflow.enums.OrderStatus;
import com.shopflow.shopflow.service.auth.DenyListService;
import com.shopflow.shopflow.service.jwt.JwtService;
import com.shopflow.shopflow.service.order.OrderService;
import org.springframework.security.core.userdetails.UserDetailsService;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private DenyListService denyListService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    void checkout_WithValidRequest_ShouldReturnCreatedAndOrderResponse() throws Exception {
        CheckoutRequest request = new CheckoutRequest("123 Main St");

        OrderItemResponse item = OrderItemResponse.builder()
                .productId(1L)
                .productName("Notebook")
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(3000))
                .subtotal(BigDecimal.valueOf(6000))
                .build();

        OrderResponse order = OrderResponse.builder()
                .id(1L)
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(6000))
                .items(List.of(item))
                .address("123 Main St")
                .createdAt(LocalDateTime.now())
                .build();

        when(orderService.checkout(any(CheckoutRequest.class))).thenReturn(order);

        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").value(6000));

        verify(orderService).checkout(any(CheckoutRequest.class));
    }

    @Test
    void getOrderById_WithExistingId_ShouldReturnOrder() throws Exception {
        OrderResponse order = OrderResponse.builder()
                .id(1L)
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(6000))
                .items(List.of())
                .address("123 Main St")
                .createdAt(LocalDateTime.now())
                .build();

        when(orderService.getOrderById(1L)).thenReturn(order);

        mockMvc.perform(get("/api/v1/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(orderService).getOrderById(1L);
    }

    @Test
    void getOrderHistory_ShouldReturnListOfOrders() throws Exception {
        OrderResponse order = OrderResponse.builder()
                .id(1L)
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(6000))
                .items(List.of())
                .address("123 Main St")
                .createdAt(LocalDateTime.now())
                .build();

        when(orderService.getOrderHistory()).thenReturn(List.of(order));

        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDING"));

        verify(orderService).getOrderHistory();
    }

    @Test
    void updateStatus_WithValidRequest_ShouldReturnUpdatedOrder() throws Exception {
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(OrderStatus.DELIVERED);

        OrderResponse order = OrderResponse.builder()
                .id(1L)
                .status(OrderStatus.DELIVERED)
                .totalAmount(BigDecimal.valueOf(6000))
                .items(List.of())
                .address("123 Main St")
                .createdAt(LocalDateTime.now())
                .build();

        when(orderService.updateStatus(anyLong(), any(UpdateOrderStatusRequest.class))).thenReturn(order);

        mockMvc.perform(patch("/api/v1/orders/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("DELIVERED"));

        verify(orderService).updateStatus(anyLong(), any(UpdateOrderStatusRequest.class));
    }

    @Test
    void getAllOrders_ShouldReturnListOfAllOrders() throws Exception {
        OrderResponse order = OrderResponse.builder()
                .id(1L)
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(6000))
                .items(List.of())
                .address("123 Main St")
                .createdAt(LocalDateTime.now())
                .build();

        when(orderService.getAllOrders()).thenReturn(List.of(order));

        mockMvc.perform(get("/api/v1/admin/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDING"));

        verify(orderService).getAllOrders();
    }
}
