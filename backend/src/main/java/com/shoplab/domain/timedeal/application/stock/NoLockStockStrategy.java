package com.shoplab.domain.timedeal.application.stock;

import com.shoplab.domain.timedeal.domain.TimeDealRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class NoLockStockStrategy implements TimeDealStockStrategy {

    private final TimeDealRepository timeDealRepository;

    @Override
    public String name() {
        return "락 없음";
    }

    @Override
    @Transactional
    public boolean tryDecrease(Long timeDealId) {
        int remaining = timeDealRepository.findRemainingQuantity(timeDealId);   // ① 읽고
        if (remaining <= 0) {
            return false;
        }
        timeDealRepository.overwriteRemaining(timeDealId, remaining - 1);      // ② 계산한 값으로 덮어쓰기
        return true;
    }
}