package com.shoplab.domain.product.dto;

import jakarta.validation.constraints.NotNull;

public record AdminVisibilityRequest(@NotNull(message = "노출 여부를 선택해 주세요.") Boolean visible) {
}