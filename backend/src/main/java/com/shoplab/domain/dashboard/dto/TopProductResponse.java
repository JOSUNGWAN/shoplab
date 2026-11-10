package com.shoplab.domain.dashboard.dto;

import com.shoplab.domain.dashboard.infrastructure.DashboardQueryRepository.TopProductRow;

public record TopProductResponse(int rank, Long productId, String productName, long quantity, long salesAmount) {

    public static TopProductResponse of(int rank, TopProductRow row) {
        return new TopProductResponse(rank, row.productId(), row.productName(), row.quantity(), row.salesAmount());
    }
}