package com.shopflow.shopflow.service.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.shopflow.shopflow.dto.dashboard.DashBoardResponse;
import com.shopflow.shopflow.entity.ProductEntity;
import com.shopflow.shopflow.repository.OrderRepository;
import com.shopflow.shopflow.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private ProductRepository productRepository;

    @InjectMocks
    private AdminServiceImpl adminService;

    @Test
    void getDashBoard_ShouldReturnAggregatedMetrics() {
        ProductEntity lowStockProduct = ProductEntity.builder()
                .id(1L).name("Notebook").stockQty(2).stockThreshold(5).build();

        when(orderRepository.count()).thenReturn(42L);
        when(orderRepository.findTodayRevenue()).thenReturn(BigDecimal.valueOf(1500.00));
        when(productRepository.findLowStockProducts()).thenReturn(List.of(lowStockProduct));

        DashBoardResponse response = adminService.getDashBoard();

        assertNotNull(response);
        assertEquals(42L, response.getTotalOrders());
        assertEquals(BigDecimal.valueOf(1500.00), response.getTodayRevenue());
        assertEquals(1, response.getLowStockProducts().size());
        assertEquals("Notebook", response.getLowStockProducts().get(0).getProductName());
        assertEquals(true, response.getLowStockProducts().get(0).getLowStock());
    }

    @Test
    void getDashBoard_WhenNoLowStockProducts_ShouldReturnEmptyList() {
        when(orderRepository.count()).thenReturn(10L);
        when(orderRepository.findTodayRevenue()).thenReturn(BigDecimal.ZERO);
        when(productRepository.findLowStockProducts()).thenReturn(List.of());

        DashBoardResponse response = adminService.getDashBoard();

        assertNotNull(response);
        assertEquals(0, response.getLowStockProducts().size());
        assertEquals(BigDecimal.ZERO, response.getTodayRevenue());
    }

    @Test
    void getDashBoard_WhenProductHasNullThreshold_ShouldMapLowStockAsFalse() {
        ProductEntity nullThresholdProduct = ProductEntity.builder()
                .id(2L).name("Mouse").stockQty(5).stockThreshold(null).build();

        when(orderRepository.count()).thenReturn(0L);
        when(orderRepository.findTodayRevenue()).thenReturn(BigDecimal.ZERO);
        when(productRepository.findLowStockProducts()).thenReturn(List.of(nullThresholdProduct));

        DashBoardResponse response = adminService.getDashBoard();

        assertEquals(false, response.getLowStockProducts().get(0).getLowStock());
    }

}
