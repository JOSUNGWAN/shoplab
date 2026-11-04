package com.shoplab.domain.product.dto;

import com.shoplab.domain.product.domain.Product;

public record AdminProductDetailResponse(
        Long id,
        String name,
        String description,
        Long categoryId,
        String categoryName,
        int price,
        int stockQuantity,
        String status,
        String thumbnailUrl
) {
    public static AdminProductDetailResponse from(Product product) {
        return new AdminProductDetailResponse(
                product.getId(), product.getName(), product.getDescription(),
                product.getCategory().getId(), product.getCategory().getName(),
                product.getPrice(), product.getStockQuantity(),
                product.getStatus().name(), product.getThumbnailUrl());
    }
}