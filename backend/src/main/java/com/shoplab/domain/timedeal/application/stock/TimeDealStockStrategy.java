package com.shoplab.domain.timedeal.application.stock;

public interface TimeDealStockStrategy {

    String name();

    /** 수량 1개 차감. 성공하면 true, 품절이면 false */
    boolean tryDecrease(Long timeDealId);
}