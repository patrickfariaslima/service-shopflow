package com.shopflow.shopflow.service.admin;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.shopflow.shopflow.dto.dashboard.DashBoardResponse;
import com.shopflow.shopflow.dto.stock.StockOverviewResponse;
import com.shopflow.shopflow.repository.OrderRepository;
import com.shopflow.shopflow.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService{
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Override
    public DashBoardResponse getDashBoard() {
        
        List<StockOverviewResponse> lowStockProducts = productRepository.findLowStockProducts().stream()
            .map(p -> StockOverviewResponse.builder()
                .productId(p.getId())
                .productName(p.getName())
                .stockQty(p.getStockQty())
                .stockThreshold(p.getStockThreshold())
                .lowStock(p.getStockQty() <= p.getStockThreshold())
                .build()
            ).toList();

        BigDecimal todayRevenue = orderRepository.findTodayRevenue();

        Long totalOrders = orderRepository.count();

        return new DashBoardResponse(totalOrders, todayRevenue, lowStockProducts);
    }
}
