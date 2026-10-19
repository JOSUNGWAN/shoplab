package com.shoplab.domain.order.domain;

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
}