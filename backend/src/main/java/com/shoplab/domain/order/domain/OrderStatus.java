package com.shoplab.domain.order.domain;

import java.util.List;

public enum OrderStatus {
    PENDING_PAYMENT, PAID, PREPARING, SHIPPING, DELIVERED, CANCELED;

    public boolean canTransitionTo(OrderStatus next) {
        return switch (this) {
            case PENDING_PAYMENT -> next == PAID || next == CANCELED;
            case PAID -> next == PREPARING || next == CANCELED;
            case PREPARING -> next == SHIPPING || next == CANCELED;
            case SHIPPING -> next == DELIVERED;
            case DELIVERED, CANCELED -> false;
        };
    }

    // 사용자가 직접 취소할 수 있는 상태
    public boolean isCancelableByMember() {
        return this == PENDING_PAYMENT || this == PAID;
    }

    // 관리자가 진행시킬 수 있는 배송 단계
    public boolean isAdminProgressTarget() {
        return this == PREPARING || this == SHIPPING || this == DELIVERED;
    }

    /** 매출로 집계하는 상태: 결제 완료 이후 (결제 대기·취소 제외) */
    public static final List<OrderStatus> SALES_STATUSES = List.of(PAID, PREPARING, SHIPPING, DELIVERED);
}