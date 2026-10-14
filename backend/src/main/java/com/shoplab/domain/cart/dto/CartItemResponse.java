package com.shoplab.domain.cart.dto;

import com.shoplab.domain.cart.domain.CartItem;
import com.shoplab.domain.product.domain.Product;
import com.shoplab.domain.product.domain.ProductStatus;

public record CartItemResponse(
        Long id,
        Long productId,
        String productName,
        String thumbnailUrl,
        int price,
        int quantity,
        long subtotal,
        int stockQuantity,
        boolean available
) {
    public static CartItemResponse from(CartItem item) {
        Product product = item.getProduct();
        boolean available = product.getStatus() == ProductStatus.ON_SALE
                && item.getQuantity() <= product.getStockQuantity();

        return new CartItemResponse(
                item.getId(), product.getId(), product.getName(), product.getThumbnailUrl(),
                product.getPrice(), item.getQuantity(),
                (long) product.getPrice() * item.getQuantity(),
                product.getStockQuantity(), available);
    }
}