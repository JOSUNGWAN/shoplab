package com.shoplab.domain.dashboard.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record DashboardRequest(
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
        @Min(value = 1, message = "순위는 1개 이상 조회해야 합니다.")
        @Max(value = 20, message = "순위는 최대 20개까지 조회할 수 있습니다.") Integer limit
) {
    // 기본값: 오늘까지 최근 14일, 상위 5개
    public DashboardRequest {
        if (to == null) to = LocalDate.now();
        if (from == null) from = to.minusDays(13);
        if (limit == null) limit = 5;
    }
}