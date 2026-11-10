package com.shoplab.domain.dashboard.dto;

public record DashboardSummaryResponse(
        long todaySalesAmount,
        long todayOrderCount,
        long yesterdaySalesAmount,
        long paidCount,          // 결제 완료 = 배송 준비를 기다리는 주문
        long preparingCount,
        long shippingCount,
        long soldOutProductCount,
        long lowStockProductCount,
        int lowStockThreshold
) {
}