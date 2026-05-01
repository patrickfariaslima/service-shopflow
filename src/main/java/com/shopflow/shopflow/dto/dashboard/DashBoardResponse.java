package com.shopflow.shopflow.dto.dashboard;

import java.math.BigDecimal;
import java.util.List;

import com.shopflow.shopflow.dto.stock.StockOverviewResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashBoardResponse {
    private Long totalOrders;
    private BigDecimal todayRevenue;
    List<StockOverviewResponse> lowStockProducts;
}
