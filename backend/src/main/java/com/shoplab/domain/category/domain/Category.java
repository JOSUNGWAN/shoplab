package com.shoplab.domain.category.domain;

import com.shoplab.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(name = "category_seq_gen", sequenceName = "category_seq", allocationSize = 50)
public class Category extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "category_seq_gen")
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @Column(nullable = false)
    private int sortOrder;

    private Category(String name, Category parent, int sortOrder) {
        this.name = name;
        this.parent = parent;
        this.sortOrder = sortOrder;
    }

    public static Category create(String name, Category parent, int sortOrder) {
        return new Category(name, parent, sortOrder);
    }
}