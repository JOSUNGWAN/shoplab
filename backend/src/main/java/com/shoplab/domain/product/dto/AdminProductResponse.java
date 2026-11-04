package com.shoplab.domain.product.dto;

import com.shoplab.domain.product.domain.Product;

public record AdminProductResponse(
        Long id,
        String name,
        Long categoryId,
        String categoryName,
        int price,
        int stockQuantity,
        String status,
        String thumbnailUrl
) {
    public static AdminProductResponse from(Product product) {
        return new AdminProductResponse(
                product.getId(), product.getName(),
                product.getCategory().getId(), product.getCategory().getName(),
                product.getPrice(), product.getStockQuantity(),
                product.getStatus().name(), product.getThumbnailUrl());
    }
}