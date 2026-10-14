package com.shoplab.domain.cart.domain;

import com.shoplab.domain.member.domain.Member;
import com.shoplab.domain.product.domain.Product;
import com.shoplab.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cart_items",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_cart_member_product",
                columnNames = {"member_id", "product_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(name = "cart_item_seq_gen", sequenceName = "cart_item_seq", allocationSize = 50)
public class CartItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cart_item_seq_gen")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int quantity;

    private CartItem(Member member, Product product, int quantity) {
        this.member = member;
        this.product = product;
        changeQuantity(quantity);
    }

    public static CartItem create(Member member, Product product, int quantity) {
        return new CartItem(member, product, quantity);
    }

    public void changeQuantity(int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
        }
        this.quantity = quantity;
    }
}