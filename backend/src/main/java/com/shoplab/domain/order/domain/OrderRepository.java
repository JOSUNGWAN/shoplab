package com.shoplab.domain.order.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // 목록: 페이징이 있으므로 items를 fetch join하지 않는다 (batch fetch로 해결)
    Page<Order> findByMemberIdOrderByOrderedAtDesc(Long memberId, Pageable pageable);

    // 상세: 한 건이라 items를 함께 가져온다
    @EntityGraph(attributePaths = "items")
    Optional<Order> findWithItemsByOrderNumberAndMemberId(String orderNumber, Long memberId);
}