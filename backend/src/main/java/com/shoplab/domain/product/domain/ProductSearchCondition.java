package com.shoplab.domain.product.domain;

import java.util.List;

public record ProductSearchCondition(
        List<Long> categoryIds,
        String keyword,
        Integer minPrice,
        Integer maxPrice,
        List<ProductStatus> statuses,
        ProductSort sort
) {
}