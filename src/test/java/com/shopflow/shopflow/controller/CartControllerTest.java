package com.shopflow.shopflow.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopflow.shopflow.dto.cart.AddCartItemRequest;
import com.shopflow.shopflow.dto.cart.CartItemResponse;
import com.shopflow.shopflow.dto.cart.CartResponse;
import com.shopflow.shopflow.dto.cart.UpdateCartItemRequest;
import com.shopflow.shopflow.service.auth.DenyListService;
import com.shopflow.shopflow.service.cart.CartService;
import com.shopflow.shopflow.service.jwt.JwtService;
import org.springframework.security.core.userdetails.UserDetailsService;

@WebMvcTest(CartController.class)
@AutoConfigureMockMvc(addFilters = false)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CartService cartService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private DenyListService denyListService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    void getCart_ShouldReturnCartResponse() throws Exception {
        CartItemResponse item = CartItemResponse.builder()
                .productId(1L)
                .productName("Notebook")
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(3000))
                .subtotal(BigDecimal.valueOf(6000))
                .build();

        CartResponse cart = CartResponse.builder()
                .items(List.of(item))
                .total(BigDecimal.valueOf(6000))
                .build();

        when(cartService.getCart()).thenReturn(cart);

        mockMvc.perform(get("/api/v1/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].productId").value(1))
                .andExpect(jsonPath("$.total").value(6000));

        verify(cartService).getCart();
    }

    @Test
    void addItem_WithValidRequest_ShouldReturnCreatedAndCartResponse() throws Exception {
        AddCartItemRequest request = new AddCartItemRequest(1L, 2);

        CartItemResponse item = CartItemResponse.builder()
                .productId(1L)
                .productName("Notebook")
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(3000))
                .subtotal(BigDecimal.valueOf(6000))
                .build();

        CartResponse cart = CartResponse.builder()
                .items(List.of(item))
                .total(BigDecimal.valueOf(6000))
                .build();

        when(cartService.addItem(any(AddCartItemRequest.class))).thenReturn(cart);

        mockMvc.perform(post("/api/v1/cart/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.items[0].productId").value(1))
                .andExpect(jsonPath("$.total").value(6000));

        verify(cartService).addItem(any(AddCartItemRequest.class));
    }

    @Test
    void updateItem_WithValidRequest_ShouldReturnUpdatedCart() throws Exception {
        UpdateCartItemRequest request = new UpdateCartItemRequest(5);

        CartItemResponse item = CartItemResponse.builder()
                .productId(1L)
                .productName("Notebook")
                .quantity(5)
                .unitPrice(BigDecimal.valueOf(3000))
                .subtotal(BigDecimal.valueOf(15000))
                .build();

        CartResponse cart = CartResponse.builder()
                .items(List.of(item))
                .total(BigDecimal.valueOf(15000))
                .build();

        when(cartService.updateItem(anyLong(), any(UpdateCartItemRequest.class))).thenReturn(cart);

        mockMvc.perform(put("/api/v1/cart/items/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity").value(5))
                .andExpect(jsonPath("$.total").value(15000));

        verify(cartService).updateItem(anyLong(), any(UpdateCartItemRequest.class));
    }

    @Test
    void removeItem_WithExistingProductId_ShouldReturnNoContent() throws Exception {
        doNothing().when(cartService).removeItem(1L);

        mockMvc.perform(delete("/api/v1/cart/items/1"))
                .andExpect(status().isNoContent());

        verify(cartService).removeItem(1L);
    }

    @Test
    void clearCart_ShouldReturnNoContent() throws Exception {
        doNothing().when(cartService).clearCart();

        mockMvc.perform(delete("/api/v1/cart"))
                .andExpect(status().isNoContent());

        verify(cartService).clearCart();
    }
}
