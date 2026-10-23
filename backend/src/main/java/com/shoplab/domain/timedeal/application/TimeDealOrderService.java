package com.shoplab.domain.timedeal.application;

import com.shoplab.domain.member.domain.MemberRepository;
import com.shoplab.domain.order.domain.Order;
import com.shoplab.domain.order.domain.OrderRepository;
import com.shoplab.domain.order.domain.OrderStatus;
import com.shoplab.domain.order.dto.OrderDetailResponse;
import com.shoplab.domain.timedeal.domain.TimeDeal;
import com.shoplab.domain.timedeal.domain.TimeDealRepository;
import com.shoplab.domain.timedeal.dto.TimeDealOrderRequest;
import com.shoplab.global.exception.BusinessException;
import com.shoplab.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TimeDealOrderService {

    private final TimeDealRepository timeDealRepository;
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final TimeDealStockService stockService;
    private final TransactionTemplate transactionTemplate;

    public OrderDetailResponse purchase(Long memberId, Long timeDealId, TimeDealOrderRequest request) {
        TimeDeal timeDeal = timeDealRepository.findWithProductById(timeDealId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TIME_DEAL_NOT_FOUND));
        if (!timeDeal.isOpen(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.TIME_DEAL_NOT_OPEN);
        }

        // ① Redis: 1인 1개 + 수량 선차감 (대부분의 요청은 여기서 끝남)
        stockService.ensureLoaded(timeDeal);
        long result = stockService.reserve(timeDeal, memberId);
        if (result == TimeDealStockService.ALREADY_PURCHASED) {
            throw new BusinessException(ErrorCode.TIME_DEAL_ALREADY_PURCHASED);
        }
        if (result == TimeDealStockService.SOLD_OUT) {
            throw new BusinessException(ErrorCode.TIME_DEAL_SOLD_OUT);
        }

        // ② DB: 최종 확인 + 수량 차감 + 주문 생성. 실패하면 Redis 차감을 되돌린다
        try {
            return transactionTemplate.execute(status -> {
                if (orderRepository.existsByMemberIdAndTimeDealIdAndStatusNot(
                        memberId, timeDealId, OrderStatus.CANCELED)) {
                    throw new BusinessException(ErrorCode.TIME_DEAL_ALREADY_PURCHASED);
                }
                if (timeDealRepository.decreaseIfAvailable(timeDealId) != 1) {
                    throw new BusinessException(ErrorCode.TIME_DEAL_SOLD_OUT);
                }
                Order order = Order.createTimeDealOrder(
                        memberRepository.getReferenceById(memberId), request.toShippingInfo(), timeDeal);
                return OrderDetailResponse.from(orderRepository.save(order));
            });
        } catch (RuntimeException e) {
            stockService.cancelReservation(timeDealId, memberId);
            throw e;
        }
    }
}