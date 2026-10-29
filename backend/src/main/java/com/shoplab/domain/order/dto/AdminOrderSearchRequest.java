package com.shoplab.domain.order.dto;

import com.shoplab.domain.order.domain.OrderStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record AdminOrderSearchRequest(
        OrderStatus status,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
        @Size(max = 50, message = "검색어는 50자 이하로 입력해 주세요.") String keyword,
        @PositiveOrZero Integer page,
        @Min(1) @Max(100) Integer size
) {
    // 기본값: 최근 30일, 20건씩
    public AdminOrderSearchRequest {
        if (to == null) to = LocalDate.now();
        if (from == null) from = to.minusDays(30);
        if (keyword != null) keyword = keyword.isBlank() ? null : keyword.trim();
        if (page == null) page = 0;
        if (size == null) size = 20;
    }
}