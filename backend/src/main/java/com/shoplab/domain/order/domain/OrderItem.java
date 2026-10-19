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

    private OrderItem(Product product, int quantity) {
        this.product = product;
        this.productName = product.getName();
        this.orderPrice = product.getPrice();
        this.quantity = quantity;
    }

    public static OrderItem create(Product product, int quantity) {
        return new OrderItem(product, quantity);
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