package com.shoplab.domain.cart.dto;

import com.shoplab.domain.cart.domain.CartItem;

import java.util.List;

public record CartResponse(List<CartItemResponse> items, int totalQuantity, long totalPrice) {

    public static CartResponse from(List<CartItem> cartItems) {
        List<CartItemResponse> items = cartItems.stream().map(CartItemResponse::from).toList();

        // 합계는 구매 가능한 상품만
        List<CartItemResponse> available = items.stream().filter(CartItemResponse::available).toList();
        int totalQuantity = available.stream().mapToInt(CartItemResponse::quantity).sum();
        long totalPrice = available.stream().mapToLong(CartItemResponse::subtotal).sum();

        return new CartResponse(items, totalQuantity, totalPrice);
    }
}