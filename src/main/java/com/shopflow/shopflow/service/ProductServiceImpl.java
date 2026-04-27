package com.shopflow.shopflow.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.shopflow.shopflow.dto.CreateProductRequest;
import com.shopflow.shopflow.dto.ProductResponse;
import com.shopflow.shopflow.dto.UpdateProductRequest;
import com.shopflow.shopflow.entity.ProductEntity;
import com.shopflow.shopflow.repository.CategoryRepository;
import com.shopflow.shopflow.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor 
public class ProductServiceImpl implements ProductService{
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

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

    @Override
    public ProductResponse findById(Long id) {
        return productRepository.findById(id)
                .map(entity -> ProductResponse.builder()
                        .id(entity.getId())
                        .name(entity.getName())
                        .description(entity.getDescription())
                        .price(entity.getPrice())
                        .stockQty(entity.getStockQty())
                        .imageUrl(entity.getImageUrl())
                        .categoryName(entity.getCategory() != null ? entity.getCategory().getName() : null)
                        .active(entity.isActive())
                        .build())
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }


    @Override
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    @Override
    public ProductResponse createProduct(CreateProductRequest product) {
        ProductEntity entity = ProductEntity.builder()
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQty(product.getStockQty())
                .imageUrl(product.getImageUrl())
                .active(true)
                .category(product.getCategoryId() != null
                         ? categoryRepository.findById(product.getCategoryId())
                         .orElseThrow(() -> new RuntimeException("Category not found")) 
                         : null)
                .createdAt(LocalDateTime.now())
                .build();
        ProductEntity savedEntity = productRepository.save(entity);

        return ProductResponse.builder()
                .id(savedEntity.getId())
                .name(savedEntity.getName())
                .description(savedEntity.getDescription())
                .price(savedEntity.getPrice())
                .stockQty(savedEntity.getStockQty())
                .imageUrl(savedEntity.getImageUrl())
                .categoryName(savedEntity.getCategory() != null ? savedEntity.getCategory().getName() : null)
                .active(savedEntity.isActive())
                .build();
    }

    @Override
    public ProductResponse updateProduct(Long id, UpdateProductRequest product) {
        ProductEntity entity = productRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        
        if (product.getName() != null) {
            entity.setName(product.getName());
        }

        if (product.getDescription() != null) {
            entity.setDescription(product.getDescription());
        }

        if (product.getPrice() != null) {
            entity.setPrice(product.getPrice());
        }

        if (product.getStockQty() != null) {
            entity.setStockQty(product.getStockQty());
        }

        if (product.getStockThreshold() != null) {
            entity.setStockThreshold(product.getStockThreshold());
        }

        if (product.getImageUrl() != null) {
            entity.setImageUrl(product.getImageUrl());
        }

        if (product.getCategoryId() != null) {
            entity.setCategory(categoryRepository.findById(product.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + product.getCategoryId())));
            
        }

        ProductEntity updatedEntity = productRepository.save(entity);

        return ProductResponse.builder()
                .id(updatedEntity.getId())
                .name(updatedEntity.getName())
                .description(updatedEntity.getDescription())
                .price(updatedEntity.getPrice())
                .stockQty(updatedEntity.getStockQty())
                .imageUrl(updatedEntity.getImageUrl())
                .categoryName(updatedEntity.getCategory() != null ? updatedEntity.getCategory().getName() : null)
                .active(updatedEntity.isActive())
                .build();
    }
}
