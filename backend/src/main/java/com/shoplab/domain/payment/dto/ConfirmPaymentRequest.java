package com.shoplab.domain.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

// successUrl로 돌아온 세 값을 프런트가 그대로 전달
public record ConfirmPaymentRequest(
        @NotBlank String paymentKey,
        @NotBlank String orderId,     // = 우리 주문번호
        @NotNull @Positive Long amount
) {
}