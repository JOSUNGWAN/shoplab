package com.shoplab.global.infra.toss;

import java.time.OffsetDateTime;

// 토스 Payment 객체 중 우리가 쓰는 필드만
public record TossPaymentResponse(
        String paymentKey,
        String orderId,
        String status,
        String method,
        long totalAmount,
        OffsetDateTime approvedAt
) {
}