package com.shoplab.domain.product.dto;

import com.shoplab.domain.product.domain.Product;

public record ProductSummaryResponse(
        Long id,
        String name,
        int price,
        String status,
        String thumbnailUrl,
        String categoryName
) {
    public static ProductSummaryResponse from(Product product) {
        return new ProductSummaryResponse(
                product.getId(), product.getName(), product.getPrice(),
                product.getStatus().name(), product.getThumbnailUrl(),
                product.getCategory().getName());
    }
}