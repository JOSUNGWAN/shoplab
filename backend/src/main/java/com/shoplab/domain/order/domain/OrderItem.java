package com.shoplab.domain.order.domain;

import com.shoplab.domain.product.domain.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(name = "order_item_seq_gen", sequenceName = "order_item_seq", allocationSize = 50)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_item_seq_gen")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // 주문 시점 스냅샷
    @Column(nullable = false, length = 100)
    private String productName;

    @Column(nullable = false)
    private int orderPrice;

    @Column(nullable = false)
    private int quantity;

    private OrderItem(Product product, int quantity, int orderPrice) {
        this.product = product;
        this.productName = product.getName();
        this.orderPrice = orderPrice;
        this.quantity = quantity;
    }

    public static OrderItem create(Product product, int quantity) {
        return new OrderItem(product, quantity, product.getPrice());
    }

    // 타임딜처럼 판매가가 다른 경우
    public static OrderItem createWithPrice(Product product, int quantity, int orderPrice) {
        return new OrderItem(product, quantity, orderPrice);
    }

    void assignOrder(Order order) {
        this.order = order;
    }

    public long getSubtotal() {
        return (long) orderPrice * quantity;
    }

    void restoreStock() {
        product.increaseStock(quantity);
    }
}