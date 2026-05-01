package com.shopflow.shopflow.service.stock;

import java.util.List;

import com.shopflow.shopflow.dto.stock.StockAdjustRequest;
import com.shopflow.shopflow.dto.stock.StockMovementResponse;
import com.shopflow.shopflow.dto.stock.StockOverviewResponse;

public interface StockService {
    List<StockOverviewResponse> getStockOverview();

    StockMovementResponse adjustStock(Long productId, StockAdjustRequest request);

    List<StockMovementResponse> getMovementHistory(Long productId);
}
