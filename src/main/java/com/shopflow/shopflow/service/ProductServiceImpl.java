package com.shopflow.shopflow.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.shopflow.shopflow.dto.ProductResponse;
import com.shopflow.shopflow.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor 
public class ProductServiceImpl implements ProductService{
    private final ProductRepository productRepository;

    @Override
    public List<ProductResponse> findAll(){
        return productRepository.findAll()
                .stream()
                .map(entity -> ProductResponse.builder()
                        .id(entity.getId())
                        .name(entity.getName())
                        .description(entity.getDescription())
                        .price(entity.getPrice())
                        .stockQty(entity.getStockQty())
                        .imageUrl(entity.getImageUrl())
                        .categoryName(entity.getCategory() != null ? entity.getCategory().getName() : null)
                        .active(entity.isActive())
                        .build()
            )
            .toList();
    }
}
