package com.shoplab.domain.product.dto;

import com.shoplab.domain.product.domain.ProductSort;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ProductSearchRequest(
        Long categoryId,
        @Size(max = 50, message = "검색어는 50자 이하로 입력해 주세요.") String keyword,
        @PositiveOrZero(message = "가격은 0 이상이어야 합니다.") Integer minPrice,
        @PositiveOrZero(message = "가격은 0 이상이어야 합니다.") Integer maxPrice,
        ProductSort sort,
        @PositiveOrZero Integer page,
        @Min(1) @Max(50) Integer size
) {
    // 값이 없으면 기본값으로 채우기
    public ProductSearchRequest {
        if (keyword != null) {
            keyword = keyword.isBlank() ? null : keyword.trim();
        }
        if (sort == null) sort = ProductSort.LATEST;
        if (page == null) page = 0;
        if (size == null) size = 12;
    }
}