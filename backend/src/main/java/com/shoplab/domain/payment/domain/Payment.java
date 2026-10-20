package com.shoplab.domain.payment.domain;

import com.shoplab.domain.order.domain.Order;
import com.shoplab.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(name = "payment_seq_gen", sequenceName = "payment_seq", allocationSize = 50)
public class Payment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "payment_seq_gen")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Column(nullable = false, unique = true, length = 200)
    private String paymentKey;

    @Column(length = 30)
    private String method;

    @Column(nullable = false)
    private long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    private LocalDateTime approvedAt;

    private LocalDateTime canceledAt;

    private Payment(Order order, String paymentKey, String method, long amount, LocalDateTime approvedAt) {
        this.order = order;
        this.paymentKey = paymentKey;
        this.method = method;
        this.amount = amount;
        this.approvedAt = approvedAt;
        this.status = PaymentStatus.DONE;
    }

    public static Payment approve(Order order, String paymentKey, String method, long amount, LocalDateTime approvedAt) {
        return new Payment(order, paymentKey, method, amount, approvedAt);
    }

    public void cancel() {
        this.status = PaymentStatus.CANCELED;
        this.canceledAt = LocalDateTime.now();
    }
}