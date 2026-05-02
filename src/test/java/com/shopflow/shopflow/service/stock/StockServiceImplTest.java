package com.shopflow.shopflow.service.stock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.shopflow.shopflow.dto.stock.StockAdjustRequest;
import com.shopflow.shopflow.dto.stock.StockMovementResponse;
import com.shopflow.shopflow.dto.stock.StockOverviewResponse;
import com.shopflow.shopflow.entity.OrderEntity;
import com.shopflow.shopflow.entity.ProductEntity;
import com.shopflow.shopflow.entity.StockMovementEntity;
import com.shopflow.shopflow.enums.MovementType;
import com.shopflow.shopflow.exception.BusinessException;
import com.shopflow.shopflow.exception.ResourceNotFoundException;
import com.shopflow.shopflow.repository.ProductRepository;
import com.shopflow.shopflow.repository.StockMovementRepository;

@ExtendWith(MockitoExtension.class)
class StockServiceImplTest {

    @Mock private ProductRepository productRepository;
    @Mock private StockMovementRepository stockMovementRepository;

    @InjectMocks
    private StockServiceImpl stockService;

    @Test
    void getStockOverview_ShouldReturnMappedList() {
        ProductEntity product = ProductEntity.builder()
                .id(1L).name("Notebook").stockQty(10).stockThreshold(5).active(true).build();

        when(productRepository.findByActiveTrue(Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of(product)));

        List<StockOverviewResponse> result = stockService.getStockOverview();

        assertEquals(1, result.size());
        assertEquals("Notebook", result.get(0).getProductName());
        assertEquals(10, result.get(0).getStockQty());
        assertEquals(false, result.get(0).getLowStock());
    }

    @Test
    void getStockOverview_WhenStockBelowThreshold_ShouldFlagLowStock() {
        ProductEntity product = ProductEntity.builder()
                .id(1L).name("Notebook").stockQty(3).stockThreshold(5).active(true).build();

        when(productRepository.findByActiveTrue(Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of(product)));

        List<StockOverviewResponse> result = stockService.getStockOverview();

        assertEquals(true, result.get(0).getLowStock());
    }

    @Test
    void adjustStock_WhenTypeIsIN_ShouldIncreaseStock() {
        ProductEntity product = ProductEntity.builder()
                .id(1L).name("Notebook").stockQty(10).price(BigDecimal.valueOf(3000)).build();
        StockAdjustRequest request = new StockAdjustRequest(5, MovementType.IN, "Purchase");

        StockMovementEntity savedMovement = StockMovementEntity.builder()
                .id(1L).product(product).type(MovementType.IN)
                .quantity(5).reason("Purchase").createdAt(LocalDateTime.now()).build();

        when(productRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);
        when(stockMovementRepository.save(any())).thenReturn(savedMovement);

        StockMovementResponse response = stockService.adjustStock(1L, request);

        assertEquals(15, product.getStockQty());
        assertEquals(MovementType.IN, response.getType());
        assertEquals(5, response.getQuantity());
        verify(productRepository).save(product);
    }

    @Test
    void adjustStock_WhenTypeIsOUT_AndStockSufficient_ShouldDecreaseStock() {
        ProductEntity product = ProductEntity.builder()
                .id(1L).name("Notebook").stockQty(10).price(BigDecimal.valueOf(3000)).build();
        StockAdjustRequest request = new StockAdjustRequest(3, MovementType.OUT, "Manual removal");

        StockMovementEntity savedMovement = StockMovementEntity.builder()
                .id(1L).product(product).type(MovementType.OUT)
                .quantity(3).reason("Manual removal").createdAt(LocalDateTime.now()).build();

        when(productRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);
        when(stockMovementRepository.save(any())).thenReturn(savedMovement);

        stockService.adjustStock(1L, request);

        assertEquals(7, product.getStockQty());
        verify(productRepository).save(product);
    }

    @Test
    void adjustStock_WhenTypeIsOUT_AndStockInsufficient_ShouldThrowBusinessException() {
        ProductEntity product = ProductEntity.builder()
                .id(1L).name("Notebook").stockQty(2).price(BigDecimal.valueOf(3000)).build();
        StockAdjustRequest request = new StockAdjustRequest(5, MovementType.OUT, "Manual removal");

        when(productRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(product));

        assertThrows(BusinessException.class, () -> stockService.adjustStock(1L, request));
    }

    @Test
    void adjustStock_WhenProductNotFound_ShouldThrowResourceNotFoundException() {
        StockAdjustRequest request = new StockAdjustRequest(5, MovementType.IN, "Purchase");

        when(productRepository.findByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> stockService.adjustStock(99L, request));
    }

    @Test
    void getMovementHistory_WhenProductExists_ShouldReturnMappedList() {
        ProductEntity product = ProductEntity.builder().id(1L).name("Notebook").build();
        StockMovementEntity movement = StockMovementEntity.builder()
                .id(1L).product(product).type(MovementType.IN)
                .quantity(10).reason("Initial stock").createdAt(LocalDateTime.now()).build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(stockMovementRepository.findByProduct(product)).thenReturn(List.of(movement));

        List<StockMovementResponse> result = stockService.getMovementHistory(1L);

        assertEquals(1, result.size());
        assertEquals(MovementType.IN, result.get(0).getType());
        assertEquals(10, result.get(0).getQuantity());
    }

    @Test
    void getMovementHistory_WhenProductNotFound_ShouldThrowResourceNotFoundException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> stockService.getMovementHistory(99L));
    }

    @Test
    void adjustStock_ShouldCreateStockMovement() {
        ProductEntity product = ProductEntity.builder()
                .id(1L).name("Notebook").stockQty(10).price(BigDecimal.valueOf(3000)).build();
        StockAdjustRequest request = new StockAdjustRequest(5, MovementType.IN, "Restock");

        StockMovementEntity savedMovement = StockMovementEntity.builder()
                .id(1L).product(product).type(MovementType.IN)
                .quantity(5).reason("Restock").createdAt(LocalDateTime.now()).build();

        when(productRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);
        when(stockMovementRepository.save(any())).thenReturn(savedMovement);

        StockMovementResponse response = stockService.adjustStock(1L, request);

        assertNotNull(response);
        verify(stockMovementRepository).save(any());
    }

    @Test
    void getMovementHistory_WhenMovementHasOrder_ShouldIncludeOrderId() {
        ProductEntity product = ProductEntity.builder().id(1L).name("Notebook").build();
        OrderEntity order = OrderEntity.builder().id(42L).build();
        StockMovementEntity movement = StockMovementEntity.builder()
                .id(1L).product(product).type(MovementType.OUT)
                .quantity(3).reason("Order #42").order(order).createdAt(LocalDateTime.now()).build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(stockMovementRepository.findByProduct(product)).thenReturn(List.of(movement));

        List<StockMovementResponse> result = stockService.getMovementHistory(1L);

        assertEquals(1, result.size());
        assertEquals(42L, result.get(0).getOrderId());
    }

    @Test
    void getStockOverview_WhenProductHasNullThreshold_ShouldFlagLowStockFalse() {
        ProductEntity product = ProductEntity.builder()
                .id(1L).name("Notebook").stockQty(5).stockThreshold(null).active(true).build();

        when(productRepository.findByActiveTrue(Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of(product)));

        List<StockOverviewResponse> result = stockService.getStockOverview();

        assertEquals(false, result.get(0).getLowStock());
    }

    @Test
    void adjustStock_WhenSavedMovementHasOrder_ShouldIncludeOrderIdInResponse() {
        ProductEntity product = ProductEntity.builder()
                .id(1L).name("Notebook").stockQty(10).price(BigDecimal.valueOf(3000)).build();
        OrderEntity order = OrderEntity.builder().id(42L).build();
        StockAdjustRequest request = new StockAdjustRequest(5, MovementType.IN, "Purchase");

        StockMovementEntity savedMovementWithOrder = StockMovementEntity.builder()
                .id(1L).product(product).type(MovementType.IN)
                .quantity(5).reason("Purchase").order(order).createdAt(LocalDateTime.now()).build();

        when(productRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);
        when(stockMovementRepository.save(any())).thenReturn(savedMovementWithOrder);

        StockMovementResponse response = stockService.adjustStock(1L, request);

        assertEquals(42L, response.getOrderId());
    }
}
