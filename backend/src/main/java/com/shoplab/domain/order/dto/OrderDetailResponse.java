package com.shoplab.domain.order.dto;

import com.shoplab.domain.order.domain.Order;
import com.shoplab.domain.order.domain.ShippingInfo;

import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailResponse(
        String orderNumber,
        String status,
        long totalAmount,
        LocalDateTime orderedAt,
        String recipient,
        String phone,
        String zipcode,
        String address1,
        String address2,
        List<OrderItemResponse> items
) {
    public static OrderDetailResponse from(Order order) {
        ShippingInfo s = order.getShippingInfo();
        return new OrderDetailResponse(
                order.getOrderNumber(), order.getStatus().name(), order.getTotalAmount(), order.getOrderedAt(),
                s.getRecipient(), s.getPhone(), s.getZipcode(), s.getAddress1(), s.getAddress2(),
                order.getItems().stream().map(OrderItemResponse::from).toList());
    }
}