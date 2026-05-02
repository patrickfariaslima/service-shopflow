package com.shopflow.shopflow.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.shopflow.shopflow.dto.stock.StockAdjustRequest;
import com.shopflow.shopflow.dto.stock.StockMovementResponse;
import com.shopflow.shopflow.dto.stock.StockOverviewResponse;
import com.shopflow.shopflow.enums.MovementType;
import com.shopflow.shopflow.service.auth.DenyListService;
import com.shopflow.shopflow.service.jwt.JwtService;
import com.shopflow.shopflow.service.stock.StockService;
import org.springframework.security.core.userdetails.UserDetailsService;

@WebMvcTest(StockController.class)
@AutoConfigureMockMvc(addFilters = false)
class StockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StockService stockService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private DenyListService denyListService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    void getStockOverview_ShouldReturnListOfStockOverview() throws Exception {
        StockOverviewResponse overview = StockOverviewResponse.builder()
                .productId(1L)
                .productName("Notebook")
                .stockQty(10)
                .stockThreshold(5)
                .lowStock(false)
                .build();

        when(stockService.getStockOverview()).thenReturn(List.of(overview));

        mockMvc.perform(get("/api/v1/admin/stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productId").value(1))
                .andExpect(jsonPath("$[0].productName").value("Notebook"))
                .andExpect(jsonPath("$[0].stockQty").value(10));

        verify(stockService).getStockOverview();
    }

    @Test
    void adjustStock_WithValidRequest_ShouldReturnCreatedAndMovementResponse() throws Exception {
        StockAdjustRequest request = new StockAdjustRequest(5, MovementType.IN, "Restock");

        StockMovementResponse movement = StockMovementResponse.builder()
                .id(1L)
                .quantity(5)
                .type(MovementType.IN)
                .reason("Restock")
                .createdAt(LocalDateTime.now())
                .build();

        when(stockService.adjustStock(anyLong(), any(StockAdjustRequest.class))).thenReturn(movement);

        mockMvc.perform(post("/api/v1/admin/stock/1/adjust")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.quantity").value(5))
                .andExpect(jsonPath("$.type").value("IN"));

        verify(stockService).adjustStock(anyLong(), any(StockAdjustRequest.class));
    }

    @Test
    void getMovementHistory_WithExistingProductId_ShouldReturnListOfMovements() throws Exception {
        StockMovementResponse movement = StockMovementResponse.builder()
                .id(1L)
                .quantity(5)
                .type(MovementType.IN)
                .reason("Restock")
                .createdAt(LocalDateTime.now())
                .build();

        when(stockService.getMovementHistory(1L)).thenReturn(List.of(movement));

        mockMvc.perform(get("/api/v1/admin/stock/1/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].quantity").value(5));

        verify(stockService).getMovementHistory(1L);
    }
}
