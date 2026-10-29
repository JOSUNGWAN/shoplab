package com.shoplab.domain.order.domain;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.time.LocalDateTime;
import java.util.List;

import static com.shoplab.domain.member.domain.QMember.member;
import static com.shoplab.domain.order.domain.QOrder.order;

@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Order> searchForAdmin(AdminOrderSearchCondition condition, Pageable pageable) {
        Predicate[] where = {
                statusEq(condition.status()),
                orderedFrom(condition.from()),
                orderedBefore(condition.to()),
                keywordMatches(condition.keyword())
        };

        List<Order> content = queryFactory
                .selectFrom(order)
                .join(order.member, member).fetchJoin()   // 회원 정보는 함께 (N+1 방지)
                .where(where)
                .orderBy(order.orderedAt.desc(), order.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        // 주문 상품(items)은 default_batch_fetch_size로 한 번에 조회

        JPAQuery<Long> countQuery = queryFactory
                .select(order.count())
                .from(order)
                .join(order.member, member)
                .where(where);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression statusEq(OrderStatus status) {
        return status == null ? null : order.status.eq(status);
    }

    private BooleanExpression orderedFrom(LocalDateTime from) {
        return from == null ? null : order.orderedAt.goe(from);
    }

    private BooleanExpression orderedBefore(LocalDateTime to) {
        return to == null ? null : order.orderedAt.lt(to);
    }

    private BooleanExpression keywordMatches(String keyword) {
        if (keyword == null) {
            return null;
        }
        return order.orderNumber.containsIgnoreCase(keyword)
                .or(order.shippingInfo.recipient.contains(keyword))
                .or(member.email.containsIgnoreCase(keyword));
    }
}