package com.shoplab.domain.order.dto;

import com.shoplab.domain.order.domain.OrderItem;

public record OrderItemResponse(Long productId, String productName, int orderPrice, int quantity, long subtotal) {

    public static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(
                item.getProduct().getId(), item.getProductName(), item.getOrderPrice(),
                item.getQuantity(), item.getSubtotal());
    }
}