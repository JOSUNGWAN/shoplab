package com.shoplab.domain.product.dto;

import com.shoplab.domain.product.domain.ProductStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record AdminProductSearchRequest(
        @Size(max = 50, message = "검색어는 50자 이하로 입력해 주세요.") String keyword,
        Long categoryId,
        ProductStatus status,
        @PositiveOrZero Integer page,
        @Min(1) @Max(100) Integer size
) {
    public AdminProductSearchRequest {
        if (keyword != null) keyword = keyword.isBlank() ? null : keyword.trim();
        if (page == null) page = 0;
        if (size == null) size = 20;
    }
}