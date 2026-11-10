package com.shoplab.domain.dashboard.dto;

import java.time.LocalDate;

public record DailySalesResponse(LocalDate date, long orderCount, long salesAmount) {

    public static DailySalesResponse empty(LocalDate date) {
        return new DailySalesResponse(date, 0, 0);
    }
}