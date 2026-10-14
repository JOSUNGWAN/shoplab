package com.shoplab.domain.cart.api;

import com.shoplab.domain.cart.application.CartService;
import com.shoplab.domain.cart.dto.AddCartItemRequest;
import com.shoplab.domain.cart.dto.CartCountResponse;
import com.shoplab.domain.cart.dto.CartItemResponse;
import com.shoplab.domain.cart.dto.CartResponse;
import com.shoplab.domain.cart.dto.UpdateCartItemRequest;
import com.shoplab.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ApiResponse<CartResponse> getCart(@AuthenticationPrincipal Long memberId) {
        return ApiResponse.ok(cartService.getCart(memberId));
    }

    @GetMapping("/count")
    public ApiResponse<CartCountResponse> getCount(@AuthenticationPrincipal Long memberId) {
        return ApiResponse.ok(cartService.getCount(memberId));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartItemResponse>> addItem(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody AddCartItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(cartService.addItem(memberId, request)));
    }

    @PatchMapping("/items/{cartItemId}")
    public ApiResponse<CartItemResponse> changeQuantity(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        return ApiResponse.ok(cartService.changeQuantity(memberId, cartItemId, request.quantity()));
    }

    @DeleteMapping("/items/{cartItemId}")
    public ApiResponse<Void> removeItem(@AuthenticationPrincipal Long memberId, @PathVariable Long cartItemId) {
        cartService.removeItem(memberId, cartItemId);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/items")
    public ApiResponse<Void> removeItems(@AuthenticationPrincipal Long memberId, @RequestParam List<Long> ids) {
        cartService.removeItems(memberId, ids);
        return ApiResponse.ok(null);
    }
}