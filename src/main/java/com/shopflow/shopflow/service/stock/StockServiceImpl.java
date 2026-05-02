package com.shopflow.shopflow.service.stock;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.shopflow.shopflow.dto.stock.StockAdjustRequest;
import com.shopflow.shopflow.dto.stock.StockMovementResponse;
import com.shopflow.shopflow.dto.stock.StockOverviewResponse;
import com.shopflow.shopflow.entity.ProductEntity;
import com.shopflow.shopflow.entity.StockMovementEntity;
import com.shopflow.shopflow.enums.MovementType;
import com.shopflow.shopflow.exception.BusinessException;
import com.shopflow.shopflow.exception.ResourceNotFoundException;
import com.shopflow.shopflow.repository.ProductRepository;
import com.shopflow.shopflow.repository.StockMovementRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService{
    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;

    private String productNotFoundText = "Product not found.";

    @Override
    public List<StockOverviewResponse> getStockOverview() {
        Page<ProductEntity> products = productRepository.findByActiveTrue(Pageable.unpaged());

        return products.stream()
                .map(product -> StockOverviewResponse.builder()
                        .productId(product.getId())
                        .productName(product.getName())
                        .stockQty(product.getStockQty())
                        .stockThreshold(product.getStockThreshold())
                        .lowStock(product.getStockThreshold() != null && product.getStockQty()  <= product.getStockThreshold())
                        .build()
                    ).toList();
    }

    @Override
    public StockMovementResponse adjustStock(Long productId, StockAdjustRequest request) {
        ProductEntity product = productRepository.findByIdAndActiveTrue(productId).orElseThrow(() -> new ResourceNotFoundException(productNotFoundText));

        if (request.getType() == MovementType.IN) {
            product.setStockQty(product.getStockQty() + request.getQuantity());
        } else {
            if (product.getStockQty() < request.getQuantity()) {
                throw new BusinessException("Insufficient stock.");
            } else {
                product.setStockQty(product.getStockQty() - request.getQuantity());
            }
        }

        ProductEntity updatedProduct = productRepository.save(product);

        StockMovementEntity movement = StockMovementEntity.builder()
                .product(updatedProduct)
                .type(request.getType())
                .quantity(request.getQuantity())
                .reason(request.getReason())
                .order(null)
                .createdAt(LocalDateTime.now())
        .build();

        StockMovementEntity savedMovement = stockMovementRepository.save(movement);

        return StockMovementResponse.builder()
                .id(savedMovement.getId())
                .type(savedMovement.getType())
                .quantity(savedMovement.getQuantity())
                .reason(savedMovement.getReason())
                .orderId(savedMovement.getOrder() != null ? savedMovement.getOrder().getId() : null)
                .createdAt(savedMovement.getCreatedAt())
                .build();
    }


    @Override
    public List<StockMovementResponse> getMovementHistory(Long productId) {
        ProductEntity product = productRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException(productNotFoundText));

        List<StockMovementEntity> movements = stockMovementRepository.findByProduct(product);


        return movements.stream()
                    .map(movement -> StockMovementResponse.builder()
                            .id(movement.getId())
                            .type(movement.getType())
                            .quantity(movement.getQuantity())
                            .reason(movement.getReason())
                            .orderId(movement.getOrder() != null ? movement.getOrder().getId() : null)
                            .createdAt(movement.getCreatedAt())
                            .build()
                        ).toList();
    }
}
