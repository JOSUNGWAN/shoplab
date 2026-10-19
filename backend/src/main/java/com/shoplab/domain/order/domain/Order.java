package com.shoplab.domain.order.domain;

import com.shoplab.domain.member.domain.Member;
import com.shoplab.global.common.BaseTimeEntity;
import com.shoplab.global.exception.BusinessException;
import com.shoplab.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders",
        indexes = @Index(name = "idx_order_member_ordered", columnList = "member_id, ordered_at"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(name = "order_seq_gen", sequenceName = "order_seq", allocationSize = 50)
public class Order extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_seq_gen")
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(nullable = false)
    private long totalAmount;

    @Embedded
    private ShippingInfo shippingInfo;

    @Column(nullable = false)
    private LocalDateTime orderedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    private Order(Member member, ShippingInfo shippingInfo, List<OrderItem> items) {
        this.orderNumber = generateOrderNumber();
        this.member = member;
        this.shippingInfo = shippingInfo;
        this.status = OrderStatus.PENDING_PAYMENT;
        this.orderedAt = LocalDateTime.now();
        items.forEach(this::addItem);
        this.totalAmount = this.items.stream().mapToLong(OrderItem::getSubtotal).sum();
    }

    public static Order create(Member member, ShippingInfo shippingInfo, List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        return new Order(member, shippingInfo, items);
    }

    private void addItem(OrderItem item) {
        items.add(item);
        item.assignOrder(this);
    }

    public void changeStatus(OrderStatus next) {
        if (!status.canTransitionTo(next)) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS);
        }
        this.status = next;
    }

    /** 사용자 취소: 결제 전·결제 완료 상태에서만 가능, 재고 복구 */
    public void cancel() {
        if (status == OrderStatus.CANCELED) {
            throw new BusinessException(ErrorCode.ORDER_ALREADY_CANCELED);
        }
        if (!status.isCancelableByMember()) {
            throw new BusinessException(ErrorCode.ORDER_CANNOT_BE_CANCELED);
        }
        changeStatus(OrderStatus.CANCELED);
        items.forEach(OrderItem::restoreStock);
    }

    // 예: 20261019-3F9A2C1B
    private static String generateOrderNumber() {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return date + "-" + random;
    }
}