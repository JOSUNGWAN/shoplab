package com.shoplab.domain.product.api;

import com.shoplab.domain.product.application.AdminProductService;
import com.shoplab.domain.product.dto.*;
import com.shoplab.global.common.ApiResponse;
import com.shoplab.global.common.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final AdminProductService adminProductService;

    @GetMapping
    public ApiResponse<PageResponse<AdminProductResponse>> search(@Valid @ModelAttribute AdminProductSearchRequest request) {
        return ApiResponse.ok(adminProductService.search(request));
    }

    @GetMapping("/{productId}")
    public ApiResponse<AdminProductDetailResponse> getProduct(@PathVariable Long productId) {
        return ApiResponse.ok(adminProductService.getProduct(productId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AdminProductDetailResponse> create(@Valid @RequestBody AdminProductCreateRequest request) {
        return ApiResponse.ok(adminProductService.create(request));
    }

    @PutMapping("/{productId}")
    public ApiResponse<AdminProductDetailResponse> update(@PathVariable Long productId,
                                                          @Valid @RequestBody AdminProductUpdateRequest request) {
        return ApiResponse.ok(adminProductService.update(productId, request));
    }

    @PatchMapping("/{productId}/stock")
    public ApiResponse<AdminProductDetailResponse> adjustStock(@PathVariable Long productId,
                                                               @Valid @RequestBody AdminStockAdjustRequest request) {
        return ApiResponse.ok(adminProductService.adjustStock(productId, request));
    }

    @PatchMapping("/{productId}/visibility")
    public ApiResponse<AdminProductDetailResponse> changeVisibility(@PathVariable Long productId,
                                                                    @Valid @RequestBody AdminVisibilityRequest request) {
        return ApiResponse.ok(adminProductService.changeVisibility(productId, request));
    }
}