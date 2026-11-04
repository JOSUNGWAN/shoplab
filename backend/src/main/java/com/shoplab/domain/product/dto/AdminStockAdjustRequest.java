package com.shoplab.domain.product.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AdminStockAdjustRequest(
        @NotNull(message = "변경할 수량을 입력해 주세요.")
        @Min(value = -99_999, message = "한 번에 99,999개까지 변경할 수 있습니다.")
        @Max(value = 99_999, message = "한 번에 99,999개까지 변경할 수 있습니다.")
        Integer delta
) {
}