package com.shoplab.domain.timedeal.application.stock;

import com.shoplab.domain.timedeal.domain.TimeDeal;
import com.shoplab.domain.timedeal.domain.TimeDealRepository;
import com.shoplab.global.exception.BusinessException;
import com.shoplab.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class OptimisticLockStockStrategy implements TimeDealStockStrategy {

    private static final int MAX_RETRY = 50;

    private final TimeDealRepository timeDealRepository;
    private final TransactionTemplate transactionTemplate;

    @Override
    public String name() {
        return "낙관적 락";
    }

    @Override
    public boolean tryDecrease(Long timeDealId) {
        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
            try {
                // 시도마다 새 트랜잭션 (실패한 트랜잭션은 재사용 불가)
                return Boolean.TRUE.equals(transactionTemplate.execute(status -> {
                    TimeDeal timeDeal = timeDealRepository.findById(timeDealId)
                            .orElseThrow(() -> new BusinessException(ErrorCode.TIME_DEAL_NOT_FOUND));
                    if (timeDeal.getRemainingQuantity() <= 0) {
                        return false;
                    }
                    timeDeal.decrease();   // UPDATE ... WHERE id = ? AND version = ?
                    return true;
                }));
            } catch (OptimisticLockingFailureException e) {
                sleepBriefly();   // 다른 요청이 먼저 바꿈 → 잠깐 쉬었다가 다시
            }
        }
        throw new BusinessException(ErrorCode.ORDER_BUSY);
    }

    private void sleepBriefly() {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextLong(5, 20));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}