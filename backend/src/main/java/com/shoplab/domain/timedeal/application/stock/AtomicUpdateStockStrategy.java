package com.shoplab.domain.timedeal.application.stock;

import com.shoplab.domain.timedeal.domain.TimeDealRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AtomicUpdateStockStrategy implements TimeDealStockStrategy {

    private final TimeDealRepository timeDealRepository;

    @Override
    public String name() {
        return "조건부 UPDATE";
    }

    @Override
    @Transactional
    public boolean tryDecrease(Long timeDealId) {
        // 조회 없이 한 문장으로: 남아 있으면 차감(1건), 없으면 0건
        return timeDealRepository.decreaseIfAvailable(timeDealId) == 1;
    }
}