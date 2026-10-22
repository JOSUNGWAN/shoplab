package com.shoplab.domain.timedeal.domain;

import com.shoplab.domain.product.domain.Product;
import com.shoplab.global.common.BaseTimeEntity;
import com.shoplab.global.exception.BusinessException;
import com.shoplab.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "time_deals")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(name = "time_deal_seq_gen", sequenceName = "time_deal_seq", allocationSize = 50)
public class TimeDeal extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "time_deal_seq_gen")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int dealPrice;

    @Column(nullable = false)
    private int totalQuantity;

    @Column(nullable = false)
    private int remainingQuantity;

    @Column(nullable = false)
    private LocalDateTime startAt;

    @Column(nullable = false)
    private LocalDateTime endAt;

    // 낙관적 락용 버전 (수정될 때마다 자동 증가)
    @Version
    private Long version;

    private TimeDeal(Product product, int dealPrice, int quantity, LocalDateTime startAt, LocalDateTime endAt) {
        if (quantity < 1 || !startAt.isBefore(endAt)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        this.product = product;
        this.dealPrice = dealPrice;
        this.totalQuantity = quantity;
        this.remainingQuantity = quantity;
        this.startAt = startAt;
        this.endAt = endAt;
    }

    public static TimeDeal create(Product product, int dealPrice, int quantity,
                                  LocalDateTime startAt, LocalDateTime endAt) {
        return new TimeDeal(product, dealPrice, quantity, startAt, endAt);
    }

    public void decrease() {
        if (remainingQuantity <= 0) {
            throw new BusinessException(ErrorCode.TIME_DEAL_SOLD_OUT);
        }
        remainingQuantity--;
    }

    public boolean isOpen(LocalDateTime now) {
        return !now.isBefore(startAt) && now.isBefore(endAt);
    }
}