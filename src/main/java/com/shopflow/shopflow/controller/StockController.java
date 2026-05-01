package com.shopflow.shopflow.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopflow.shopflow.dto.stock.StockAdjustRequest;
import com.shopflow.shopflow.dto.stock.StockMovementResponse;
import com.shopflow.shopflow.dto.stock.StockOverviewResponse;
import com.shopflow.shopflow.service.stock.StockService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
@RequestMapping("/api/v1/admin/stock")
@RequiredArgsConstructor
public class StockController {
    private final StockService stockService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<StockOverviewResponse>> getStockOverview() {
        return ResponseEntity.ok(stockService.getStockOverview());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{productId}/adjust")
    public ResponseEntity<StockMovementResponse> adjustStock(@PathVariable Long productId, @Valid @RequestBody StockAdjustRequest request) {
        StockMovementResponse response = stockService.adjustStock(productId, request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{productId}/history")
    public ResponseEntity<List<StockMovementResponse>> getMovementHistory(@PathVariable Long productId) {
        return ResponseEntity.ok(stockService.getMovementHistory(productId));
    }
    
    
    
}
