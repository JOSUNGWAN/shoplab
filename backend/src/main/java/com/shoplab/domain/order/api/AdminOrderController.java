package com.shoplab.domain.order.api;

import com.shoplab.domain.order.application.AdminOrderService;
import com.shoplab.domain.order.dto.AdminBulkStatusResponse;
import com.shoplab.domain.order.dto.AdminOrderResponse;
import com.shoplab.domain.order.dto.AdminOrderSearchRequest;
import com.shoplab.domain.order.dto.AdminOrderStatusRequest;
import com.shoplab.domain.order.dto.OrderDetailResponse;
import com.shoplab.global.common.ApiResponse;
import com.shoplab.global.common.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    @GetMapping
    public ApiResponse<PageResponse<AdminOrderResponse>> search(@Valid @ModelAttribute AdminOrderSearchRequest request) {
        return ApiResponse.ok(adminOrderService.search(request));
    }

    @GetMapping("/{orderNumber}")
    public ApiResponse<OrderDetailResponse> getOrder(@PathVariable String orderNumber) {
        return ApiResponse.ok(adminOrderService.getOrder(orderNumber));
    }

    @PatchMapping("/status")
    public ApiResponse<AdminBulkStatusResponse> changeStatus(@Valid @RequestBody AdminOrderStatusRequest request) {
        return ApiResponse.ok(adminOrderService.changeStatus(request));
    }
}