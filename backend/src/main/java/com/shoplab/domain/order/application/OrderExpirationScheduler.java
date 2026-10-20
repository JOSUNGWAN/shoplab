package com.shoplab.domain.order.application;

import com.shoplab.domain.order.domain.Order;
import com.shoplab.domain.order.domain.OrderRepository;
import com.shoplab.domain.order.domain.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderExpirationScheduler {

    private static final int BATCH_SIZE = 100;

    private final OrderRepository orderRepository;
    private final OrderService orderService;

    // 이전 실행이 끝나고 1분 뒤에 다시 실행
    @Scheduled(fixedDelay = 60_000, initialDelay = 30_000)
    public void cancelExpiredOrders() {
        LocalDateTime before = LocalDateTime.now().minus(Order.PAYMENT_TIMEOUT);
        List<Long> orderIds = orderRepository.findIdsByStatusAndOrderedAtBefore(
                OrderStatus.PENDING_PAYMENT, before, PageRequest.of(0, BATCH_SIZE));

        int canceled = 0;
        for (Long orderId : orderIds) {
            try {
                orderService.expireOrder(orderId);   // 주문마다 별도 트랜잭션
                canceled++;
            } catch (Exception e) {
                log.warn("미결제 주문 자동 취소 실패. orderId={}", orderId, e);
            }
        }
        if (canceled > 0) {
            log.info("미결제 주문 {}건 자동 취소", canceled);
        }
    }
}