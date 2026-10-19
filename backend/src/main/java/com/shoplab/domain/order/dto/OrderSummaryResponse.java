package com.shoplab.domain.order.dto;

import com.shoplab.domain.order.domain.Order;
import com.shoplab.domain.order.domain.OrderItem;

import java.time.LocalDateTime;
import java.util.List;

public record OrderSummaryResponse(
        String orderNumber,
        String status,
        long totalAmount,
        LocalDateTime orderedAt,
        String title,
        int itemCount
) {
    public static OrderSummaryResponse from(Order order) {
        List<OrderItem> items = order.getItems();
        String first = items.getFirst().getProductName();
        String title = items.size() > 1 ? first + " 외 " + (items.size() - 1) + "건" : first;
        return new OrderSummaryResponse(
                order.getOrderNumber(), order.getStatus().name(), order.getTotalAmount(),
                order.getOrderedAt(), title, items.size());
    }
}