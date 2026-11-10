package com.shoplab.domain.dashboard.api;

import com.shoplab.domain.dashboard.application.AdminDashboardService;
import com.shoplab.domain.dashboard.dto.DailySalesResponse;
import com.shoplab.domain.dashboard.dto.DashboardRequest;
import com.shoplab.domain.dashboard.dto.DashboardSummaryResponse;
import com.shoplab.domain.dashboard.dto.TopProductResponse;
import com.shoplab.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping("/summary")
    public ApiResponse<DashboardSummaryResponse> summary() {
        return ApiResponse.ok(adminDashboardService.getSummary());
    }

    @GetMapping("/daily-sales")
    public ApiResponse<List<DailySalesResponse>> dailySales(@Valid @ModelAttribute DashboardRequest request) {
        return ApiResponse.ok(adminDashboardService.getDailySales(request));
    }

    @GetMapping("/top-products")
    public ApiResponse<List<TopProductResponse>> topProducts(@Valid @ModelAttribute DashboardRequest request) {
        return ApiResponse.ok(adminDashboardService.getTopProducts(request));
    }
}