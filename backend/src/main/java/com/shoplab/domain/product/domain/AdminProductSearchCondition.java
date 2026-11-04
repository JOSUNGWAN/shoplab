package com.shoplab.domain.product.domain;

public record AdminProductSearchCondition(
        String keyword,
        Long categoryId,
        ProductStatus status
) {
}