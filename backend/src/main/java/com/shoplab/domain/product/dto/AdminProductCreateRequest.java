package com.shoplab.domain.product.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record AdminProductCreateRequest(
        @NotNull(message = "카테고리를 선택해 주세요.") Long categoryId,
        @NotBlank(message = "상품명을 입력해 주세요.") @Size(max = 100, message = "상품명은 100자 이하로 입력해 주세요.") String name,
        @NotBlank(message = "상품 설명을 입력해 주세요.")
        @Size(max = 5000, message = "상품 설명은 5000자 이하로 입력해 주세요.") String description,
        @NotNull(message = "가격을 입력해 주세요.") @Min(value = 100, message = "가격은 100원 이상이어야 합니다.")
        @Max(value = 10_000_000, message = "가격은 1천만 원 이하여야 합니다.") Integer price,
        @NotNull(message = "재고를 입력해 주세요.") @Min(value = 0, message = "재고는 0개 이상이어야 합니다.")
        @Max(value = 99_999, message = "재고는 99,999개 이하여야 합니다.") Integer stockQuantity,
        @URL(message = "이미지 주소 형식이 올바르지 않습니다.") @Size(max = 500) String thumbnailUrl
) {
}