package com.shoplab.domain.payment.application;

import com.shoplab.domain.cart.domain.CartItemRepository;
import com.shoplab.domain.order.domain.Order;
import com.shoplab.domain.order.domain.OrderRepository;
import com.shoplab.domain.order.domain.OrderStatus;
import com.shoplab.domain.order.dto.OrderDetailResponse;
import com.shoplab.domain.payment.domain.Payment;
import com.shoplab.domain.payment.domain.PaymentRepository;
import com.shoplab.domain.payment.dto.ConfirmPaymentRequest;
import com.shoplab.domain.product.application.ProductLockManager;
import com.shoplab.global.exception.BusinessException;
import com.shoplab.global.exception.ErrorCode;
import com.shoplab.global.infra.toss.TossPaymentResponse;
import com.shoplab.global.infra.toss.TossPaymentsClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductLockManager productLockManager;
    private final TossPaymentsClient tossPaymentsClient;
    private final TransactionTemplate transactionTemplate;

    /** 결제 승인: ①검증(짧은 트랜잭션) → ②토스 승인(트랜잭션 밖) → ③결과 반영(트랜잭션) */
    public OrderDetailResponse confirm(Long memberId, ConfirmPaymentRequest request) {
        // ① 검증
        Long orderId = transactionTemplate.execute(status -> {
            Order order = findMyOrder(memberId, request.orderId());
            if (order.getStatus() == OrderStatus.PAID) {
                return order.getId(); // 이미 결제됨 → ③에서 그대로 응답 (중복 요청)
            }
            validatePayable(order, request.amount());
            return order.getId();
        });

        // ② 토스 승인 (외부 API는 트랜잭션 밖에서)
        TossPaymentResponse toss = tossPaymentsClient.confirm(
                request.paymentKey(), request.orderId(), request.amount());

        // ③ 결과 반영. 실패하면 토스 결제를 되돌린다 (보상 트랜잭션)
        try {
            return transactionTemplate.execute(status -> completePayment(orderId, memberId, toss));
        } catch (RuntimeException e) {
            log.error("결제 반영 실패, 보상 취소 진행. orderId={}, paymentKey={}", orderId, toss.paymentKey(), e);
            tossPaymentsClient.cancelQuietly(toss.paymentKey(), "주문 처리 실패로 인한 자동 취소");
            throw e;
        }
    }

    /** 사용자 주문 취소: 결제된 주문이면 환불까지 */
    public OrderDetailResponse cancelOrder(Long memberId, String orderNumber) {
        // ① 검증 + 결제 정보 확인
        CancelTarget target = transactionTemplate.execute(status -> {
            Order order = findMyOrder(memberId, orderNumber);
            if (order.getStatus() == OrderStatus.CANCELED) {
                throw new BusinessException(ErrorCode.ORDER_ALREADY_CANCELED);
            }
            if (!order.getStatus().isCancelableByMember()) {
                throw new BusinessException(ErrorCode.ORDER_CANNOT_BE_CANCELED);
            }
            String paymentKey = paymentRepository.findByOrderId(order.getId())
                    .map(Payment::getPaymentKey).orElse(null);
            return new CancelTarget(order.getId(), paymentKey);
        });

        // ② 결제된 주문이면 토스 환불 (트랜잭션 밖)
        if (target.paymentKey() != null) {
            tossPaymentsClient.cancel(target.paymentKey(), "고객 요청 취소");
        }

        // ③ 주문 취소 + 재고 복구 + 결제 상태 변경
        return transactionTemplate.execute(status -> {
            Order order = orderRepository.findByIdForUpdate(target.orderId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
            productLockManager.lock(order.getProductIds());
            order.cancel();
            paymentRepository.findByOrderId(order.getId()).ifPresent(Payment::cancel);
            return OrderDetailResponse.from(order);
        });
    }

    private OrderDetailResponse completePayment(Long orderId, Long memberId, TossPaymentResponse toss) {
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        // 같은 결제가 이미 반영됨 (버튼 두 번 클릭 등) → 성공으로 응답, 보상 취소하면 안 됨
        if (order.getStatus() == OrderStatus.PAID && paymentRepository.existsByPaymentKey(toss.paymentKey())) {
            return OrderDetailResponse.from(order);
        }
        // 그사이 자동 취소됨 → 예외 → 호출한 쪽에서 보상 취소
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new BusinessException(ErrorCode.ORDER_EXPIRED);
        }
        // 토스가 승인한 금액도 한 번 더 확인
        if (toss.totalAmount() != order.getTotalAmount()) {
            throw new BusinessException(ErrorCode.AMOUNT_MISMATCH);
        }

        order.markPaid();
        LocalDateTime approvedAt = toss.approvedAt() != null ? toss.approvedAt().toLocalDateTime() : LocalDateTime.now();
        paymentRepository.save(Payment.approve(order, toss.paymentKey(), toss.method(), toss.totalAmount(), approvedAt));
        cartItemRepository.deleteByMemberIdAndProductIds(memberId, order.getProductIds());

        return OrderDetailResponse.from(order);
    }

    private void validatePayable(Order order, long amount) {
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS);
        }
        if (order.isPaymentExpired(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.ORDER_EXPIRED);
        }
        // 금액 위변조 방지: 브라우저가 보낸 금액이 아니라 DB의 주문 금액이 기준
        if (amount != order.getTotalAmount()) {
            throw new BusinessException(ErrorCode.AMOUNT_MISMATCH);
        }
    }

    private Order findMyOrder(Long memberId, String orderNumber) {
        return orderRepository.findWithItemsByOrderNumberAndMemberId(orderNumber, memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
    }

    private record CancelTarget(Long orderId, String paymentKey) {
    }
}