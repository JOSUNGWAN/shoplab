package com.shoplab.domain.product.dto;

import com.shoplab.domain.product.domain.Product;

public record ProductDetailResponse(
        Long id,
        String name,
        String description,
        int price,
        int stockQuantity,
        String status,
        String thumbnailUrl,
        Long categoryId,
        String categoryName
) {
    public static ProductDetailResponse from(Product product) {
        return new ProductDetailResponse(
                product.getId(), product.getName(), product.getDescription(),
                product.getPrice(), product.getStockQuantity(), product.getStatus().name(),
                product.getThumbnailUrl(),
                product.getCategory().getId(), product.getCategory().getName());
    }
}