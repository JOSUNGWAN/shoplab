package com.shoplab.domain.product.api;

import com.shoplab.domain.product.application.ProductService;
import com.shoplab.domain.product.dto.ProductDetailResponse;
import com.shoplab.domain.product.dto.ProductSummaryResponse;
import com.shoplab.global.common.ApiResponse;
import com.shoplab.global.common.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ApiResponse<PageResponse<ProductSummaryResponse>> getProducts(
            @RequestParam(required = false) Long categoryId,
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.ok(productService.getProducts(categoryId, pageable));
    }

    @GetMapping("/{productId}")
    public ApiResponse<ProductDetailResponse> getProduct(@PathVariable Long productId) {
        return ApiResponse.ok(productService.getProduct(productId));
    }
}