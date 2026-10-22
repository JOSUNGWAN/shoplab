package com.shoplab.domain.timedeal.application.stock;

import com.shoplab.domain.timedeal.domain.TimeDeal;
import com.shoplab.domain.timedeal.domain.TimeDealRepository;
import com.shoplab.global.exception.BusinessException;
import com.shoplab.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PessimisticLockStockStrategy implements TimeDealStockStrategy {

    private final TimeDealRepository timeDealRepository;

    @Override
    public String name() {
        return "비관적 락";
    }

    @Override
    @Transactional
    public boolean tryDecrease(Long timeDealId) {
        TimeDeal timeDeal = timeDealRepository.findByIdForUpdate(timeDealId)   // 행 잠금
                .orElseThrow(() -> new BusinessException(ErrorCode.TIME_DEAL_NOT_FOUND));
        if (timeDeal.getRemainingQuantity() <= 0) {
            return false;
        }
        timeDeal.decrease();   // 커밋할 때 UPDATE, 그때까지 다른 요청은 대기
        return true;
    }
}