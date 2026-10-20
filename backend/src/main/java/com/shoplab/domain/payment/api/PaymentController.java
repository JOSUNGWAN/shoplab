package com.shoplab.domain.payment.api;

import com.shoplab.domain.order.dto.OrderDetailResponse;
import com.shoplab.domain.payment.application.PaymentService;
import com.shoplab.domain.payment.dto.ConfirmPaymentRequest;
import com.shoplab.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/confirm")
    public ApiResponse<OrderDetailResponse> confirm(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody ConfirmPaymentRequest request) {
        return ApiResponse.ok(paymentService.confirm(memberId, request));
    }
}