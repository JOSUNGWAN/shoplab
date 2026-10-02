package com.shoplab.domain.product.domain;

import com.shoplab.domain.category.domain.Category;
import com.shoplab.global.common.BaseTimeEntity;
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
}