package com.shoplab.domain.product.domain;

import com.shoplab.domain.category.domain.Category;
import com.shoplab.global.common.BaseTimeEntity;
import com.shoplab.global.exception.BusinessException;
import com.shoplab.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products",
        indexes = @Index(name = "idx_product_category_status", columnList = "category_id, status"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(name = "product_seq_gen", sequenceName = "product_seq", allocationSize = 50)
public class Product extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_seq_gen")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 100)
    private String name;

    @Lob
    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private int stockQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status;

    @Column(length = 500)
    private String thumbnailUrl;

    private Product(Category category, String name, String description, int price,
                    int stockQuantity, String thumbnailUrl) {
        this.category = category;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.thumbnailUrl = thumbnailUrl;
        this.status = stockQuantity > 0 ? ProductStatus.ON_SALE : ProductStatus.SOLD_OUT;
    }

    public static Product create(Category category, String name, String description, int price,
                                 int stockQuantity, String thumbnailUrl) {
        return new Product(category, name, description, price, stockQuantity, thumbnailUrl);
    }

    public void decreaseStock(int quantity) {
        if (this.stockQuantity < quantity) {
            throw new BusinessException(ErrorCode.OUT_OF_STOCK);
        }
        this.stockQuantity -= quantity;
        syncStatusWithStock();
    }

    public void increaseStock(int quantity) {
        this.stockQuantity += quantity;
        syncStatusWithStock();
    }

    /** 관리자: 상품 정보 수정 (재고·상태는 별도 메서드로만 변경) */
    public void update(Category category, String name, String description, int price, String thumbnailUrl) {
        this.category = category;
        this.name = name;
        this.description = description;
        this.price = price;
        this.thumbnailUrl = thumbnailUrl;
    }

    /** 관리자: 재고 입고(+) / 차감(-) */
    public void adjustStock(int delta) {
        if (delta == 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "변경할 수량을 입력해 주세요.");
        }
        if (this.stockQuantity + delta < 0) {
            throw new BusinessException(ErrorCode.INVALID_STOCK_ADJUSTMENT);
        }
        this.stockQuantity += delta;
        syncStatusWithStock();
    }

    /** 관리자: 숨김 (판매 중·품절 모두 숨길 수 있음) */
    public void hide() {
        this.status = ProductStatus.HIDDEN;
    }

    /** 관리자: 다시 보이기 (재고에 따라 판매 중 또는 품절) */
    public void show() {
        if (this.status != ProductStatus.HIDDEN) {
            return;
        }
        this.status = this.stockQuantity > 0 ? ProductStatus.ON_SALE : ProductStatus.SOLD_OUT;
    }

    /** 재고에 맞춰 판매 중/품절 결정. 숨긴 상품은 관리자가 직접 보이기 전까지 숨김 유지 */
    private void syncStatusWithStock() {
        if (this.status == ProductStatus.HIDDEN) {
            return;
        }
        this.status = this.stockQuantity > 0 ? ProductStatus.ON_SALE : ProductStatus.SOLD_OUT;
    }
}