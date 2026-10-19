package com.shoplab.domain.order.api;

import com.shoplab.domain.order.application.OrderService;
import com.shoplab.domain.order.dto.CreateOrderRequest;
import com.shoplab.domain.order.dto.OrderDetailResponse;
import com.shoplab.domain.order.dto.OrderSummaryResponse;
import com.shoplab.global.common.ApiResponse;
import com.shoplab.global.common.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderDetailResponse>> createOrder(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(orderService.createOrder(memberId, request)));
    }

    @GetMapping
    public ApiResponse<PageResponse<OrderSummaryResponse>> getMyOrders(
            @AuthenticationPrincipal Long memberId,
            @RequestParam(defaultValue = "0") @PositiveOrZero int page) {
        return ApiResponse.ok(orderService.getMyOrders(memberId, page));
    }

    @GetMapping("/{orderNumber}")
    public ApiResponse<OrderDetailResponse> getMyOrder(
            @AuthenticationPrincipal Long memberId, @PathVariable String orderNumber) {
        return ApiResponse.ok(orderService.getMyOrder(memberId, orderNumber));
    }

    @PostMapping("/{orderNumber}/cancel")
    public ApiResponse<OrderDetailResponse> cancel(
            @AuthenticationPrincipal Long memberId, @PathVariable String orderNumber) {
        return ApiResponse.ok(orderService.cancel(memberId, orderNumber));
    }
}