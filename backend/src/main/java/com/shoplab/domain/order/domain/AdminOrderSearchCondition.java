package com.shoplab.domain.order.domain;

import java.time.LocalDateTime;

public record AdminOrderSearchCondition(
        OrderStatus status,
        LocalDateTime from,   // 이상
        LocalDateTime to,     // 미만
        String keyword        // 주문번호·받는 분·회원 이메일
) {
}