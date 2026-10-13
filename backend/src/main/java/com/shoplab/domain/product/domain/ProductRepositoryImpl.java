package com.shoplab.domain.product.domain;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

import static com.shoplab.domain.category.domain.QCategory.category;
import static com.shoplab.domain.product.domain.QProduct.product;

@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Product> search(ProductSearchCondition condition, Pageable pageable) {
        Predicate[] where = conditions(condition);

        List<Product> content = queryFactory
                .selectFrom(product)
                .join(product.category, category).fetchJoin()   // N+1 방지
                .where(where)
                .orderBy(orderBy(condition.sort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 개수 조회는 조인·정렬 없이 가볍게
        JPAQuery<Long> countQuery = queryFactory
                .select(product.count())
                .from(product)
                .where(where);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private Predicate[] conditions(ProductSearchCondition c) {
        return new Predicate[]{
                statusIn(c.statuses()),
                categoryIn(c.categoryIds()),
                nameContains(c.keyword()),
                priceGoe(c.minPrice()),
                priceLoe(c.maxPrice())
        };
    }

    // 값이 없으면 null을 반환 → QueryDSL이 where에서 자동으로 무시
    private BooleanExpression statusIn(List<ProductStatus> statuses) {
        return (statuses == null || statuses.isEmpty()) ? null : product.status.in(statuses);
    }

    private BooleanExpression categoryIn(List<Long> categoryIds) {
        return (categoryIds == null || categoryIds.isEmpty()) ? null : product.category.id.in(categoryIds);
    }

    private BooleanExpression nameContains(String keyword) {
        return keyword == null ? null : product.name.containsIgnoreCase(keyword);
    }

    private BooleanExpression priceGoe(Integer minPrice) {
        return minPrice == null ? null : product.price.goe(minPrice);
    }

    private BooleanExpression priceLoe(Integer maxPrice) {
        return maxPrice == null ? null : product.price.loe(maxPrice);
    }

    private OrderSpecifier<?>[] orderBy(ProductSort sort) {
        return switch (sort) {
            case PRICE_ASC -> new OrderSpecifier<?>[]{product.price.asc(), product.id.desc()};
            case PRICE_DESC -> new OrderSpecifier<?>[]{product.price.desc(), product.id.desc()};
            case LATEST -> new OrderSpecifier<?>[]{product.createdAt.desc(), product.id.desc()};
        };
    }
}