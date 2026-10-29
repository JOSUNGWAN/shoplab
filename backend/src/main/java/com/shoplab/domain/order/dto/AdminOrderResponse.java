package com.shoplab.domain.order.dto;

import com.shoplab.domain.member.domain.Member;
import com.shoplab.domain.order.domain.Order;
import com.shoplab.domain.order.domain.OrderItem;

import java.time.LocalDateTime;
import java.util.List;

public record AdminOrderResponse(
        String orderNumber,
        String status,
        long totalAmount,
        LocalDateTime orderedAt,
        String title,
        int itemCount,
        String memberEmail,
        String memberName,
        String recipient,
        String phone,
        boolean timeDeal
) {
    public static AdminOrderResponse from(Order order) {
        List<OrderItem> items = order.getItems();
        String first = items.getFirst().getProductName();
        String title = items.size() > 1 ? first + " 외 " + (items.size() - 1) + "건" : first;
        Member member = order.getMember();

        return new AdminOrderResponse(
                order.getOrderNumber(), order.getStatus().name(), order.getTotalAmount(), order.getOrderedAt(),
                title, items.size(), member.getEmail(), member.getName(),
                order.getShippingInfo().getRecipient(), order.getShippingInfo().getPhone(),
                order.isTimeDealOrder());
    }
}