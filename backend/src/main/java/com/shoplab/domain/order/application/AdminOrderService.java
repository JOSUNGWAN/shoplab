package com.shoplab.domain.order.application;

import com.shoplab.domain.order.domain.AdminOrderSearchCondition;
import com.shoplab.domain.order.domain.Order;
import com.shoplab.domain.order.domain.OrderRepository;
import com.shoplab.domain.order.dto.AdminBulkStatusResponse;
import com.shoplab.domain.order.dto.AdminOrderResponse;
import com.shoplab.domain.order.dto.AdminOrderSearchRequest;
import com.shoplab.domain.order.dto.AdminOrderStatusRequest;
import com.shoplab.domain.order.dto.OrderDetailResponse;
import com.shoplab.global.common.PageResponse;
import com.shoplab.global.exception.BusinessException;
import com.shoplab.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminOrderService {

    private final OrderRepository orderRepository;
    private final TransactionTemplate transactionTemplate;

    @Transactional(readOnly = true)
    public PageResponse<AdminOrderResponse> search(AdminOrderSearchRequest request) {
        if (request.from().isAfter(request.to())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "시작일이 종료일보다 늦을 수 없습니다.");
        }
        AdminOrderSearchCondition condition = new AdminOrderSearchCondition(
                request.status(),
                request.from().atStartOfDay(),
                request.to().plusDays(1).atStartOfDay(),   // 종료일 다음 날 0시 미만
                request.keyword());

        return PageResponse.from(orderRepository
                .searchForAdmin(condition, PageRequest.of(request.page(), request.size()))
                .map(AdminOrderResponse::from));
    }

    @Transactional(readOnly = true)
    public OrderDetailResponse getOrder(String orderNumber) {
        return orderRepository.findWithItemsAndMemberByOrderNumber(orderNumber)
                .map(OrderDetailResponse::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
    }

    /** 일괄 상태 변경: 주문마다 별도 트랜잭션, 실패한 건은 이유와 함께 반환 */
    public AdminBulkStatusResponse changeStatus(AdminOrderStatusRequest request) {
        if (!request.status().isAdminProgressTarget()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT,
                    "관리자는 배송 준비·배송 중·배송 완료로만 변경할 수 있습니다.");
        }

        int successCount = 0;
        List<AdminBulkStatusResponse.Failure> failures = new ArrayList<>();

        for (String orderNumber : new LinkedHashSet<>(request.orderNumbers())) {   // 중복 제거, 순서 유지
            try {
                transactionTemplate.executeWithoutResult(status -> {
                    Order order = orderRepository.findByOrderNumberForUpdate(orderNumber)
                            .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
                    order.progressByAdmin(request.status());
                });
                successCount++;
            } catch (BusinessException e) {
                failures.add(new AdminBulkStatusResponse.Failure(orderNumber, e.getMessage()));
            } catch (PessimisticLockingFailureException e) {
                failures.add(new AdminBulkStatusResponse.Failure(orderNumber, ErrorCode.ORDER_BUSY.getMessage()));
            }
        }

        log.info("관리자 주문 상태 일괄 변경: {} → 성공 {}건, 실패 {}건",
                request.status(), successCount, failures.size());
        return new AdminBulkStatusResponse(successCount, failures);
    }
}