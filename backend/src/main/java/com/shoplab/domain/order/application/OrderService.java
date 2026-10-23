package com.shoplab.domain.order.application;

import com.shoplab.domain.member.domain.MemberRepository;
import com.shoplab.domain.order.domain.Order;
import com.shoplab.domain.order.domain.OrderItem;
import com.shoplab.domain.order.domain.OrderRepository;
import com.shoplab.domain.order.domain.ShippingInfo;
import com.shoplab.domain.order.dto.CreateOrderRequest;
import com.shoplab.domain.order.dto.OrderDetailResponse;
import com.shoplab.domain.order.dto.OrderItemRequest;
import com.shoplab.domain.order.dto.OrderSummaryResponse;
import com.shoplab.domain.product.application.ProductLockManager;
import com.shoplab.domain.product.domain.Product;
import com.shoplab.domain.product.domain.ProductRepository;
import com.shoplab.domain.product.domain.ProductStatus;
import com.shoplab.domain.timedeal.application.TimeDealStockService;
import com.shoplab.global.common.PageResponse;
import com.shoplab.global.exception.BusinessException;
import com.shoplab.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private static final int PAGE_SIZE = 10;

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final ProductLockManager productLockManager;
    private final TimeDealStockService timeDealStockService;

    @Transactional
    public OrderDetailResponse createOrder(Long memberId, CreateOrderRequest request) {
        // 1. 같은 상품이 여러 줄로 오면 수량 합치기
        Map<Long, Integer> quantities = new LinkedHashMap<>();
        for (OrderItemRequest item : request.items()) {
            quantities.merge(item.productId(), item.quantity(), Integer::sum);
        }

        // 2. 상품 행 잠금 (id 순서)
        List<Product> products = productRepository.findAllByIdInForUpdate(quantities.keySet());
        if (products.size() != quantities.size()) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        // 3. 검증 + 재고 차감 + 스냅샷
        List<OrderItem> orderItems = new ArrayList<>();
        for (Product product : products) {
            if (product.getStatus() != ProductStatus.ON_SALE) {
                throw new BusinessException(ErrorCode.PRODUCT_NOT_ON_SALE);
            }
            int quantity = quantities.get(product.getId());
            product.decreaseStock(quantity);
            orderItems.add(OrderItem.create(product, quantity));
        }

        // 4. 주문 저장
        ShippingInfo shippingInfo = new ShippingInfo(
                request.recipient(), request.phone(), request.zipcode(), request.address1(), request.address2());
        Order order = Order.create(memberRepository.getReferenceById(memberId), shippingInfo, orderItems);

        return OrderDetailResponse.from(orderRepository.save(order));
    }

    public PageResponse<OrderSummaryResponse> getMyOrders(Long memberId, int page) {
        return PageResponse.from(
                orderRepository.findByMemberIdOrderByOrderedAtDesc(memberId, PageRequest.of(page, PAGE_SIZE))
                        .map(OrderSummaryResponse::from));
    }

    public OrderDetailResponse getMyOrder(Long memberId, String orderNumber) {
        return OrderDetailResponse.from(findMyOrder(memberId, orderNumber));
    }

    /** 스케줄러용: 결제 시간이 지난 주문 하나를 자동 취소 (주문마다 별도 트랜잭션) */
    @Transactional
    public void expireOrder(Long orderId) {
        Order order = orderRepository.findByIdForUpdate(orderId).orElse(null);
        if (order == null || !order.isPaymentExpired(LocalDateTime.now())) {
            return;
        }
        productLockManager.lock(order.getProductIds());
        order.expire();
        if (order.isTimeDealOrder()) {
            timeDealStockService.release(order.getTimeDeal().getId(), order.getMember().getId());
        }
    }

    private Order findMyOrder(Long memberId, String orderNumber) {
        return orderRepository.findWithItemsByOrderNumberAndMemberId(orderNumber, memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
    }
}