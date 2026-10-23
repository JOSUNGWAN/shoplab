package com.shoplab.domain.timedeal.application;

import com.shoplab.domain.timedeal.domain.TimeDealRepository;
import com.shoplab.domain.timedeal.dto.TimeDealResponse;
import com.shoplab.global.exception.BusinessException;
import com.shoplab.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TimeDealQueryService {

    private final TimeDealRepository timeDealRepository;

    /** 진행 중이거나 예정된 타임딜 */
    public List<TimeDealResponse> getTimeDeals() {
        LocalDateTime now = LocalDateTime.now();
        return timeDealRepository.findByEndAtAfterOrderByStartAtAsc(now).stream()
                .map(deal -> TimeDealResponse.from(deal, now))
                .toList();
    }

    public TimeDealResponse getTimeDeal(Long timeDealId) {
        return timeDealRepository.findWithProductById(timeDealId)
                .map(deal -> TimeDealResponse.from(deal, LocalDateTime.now()))
                .orElseThrow(() -> new BusinessException(ErrorCode.TIME_DEAL_NOT_FOUND));
    }
}