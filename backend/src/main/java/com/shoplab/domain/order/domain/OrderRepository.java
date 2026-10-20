package com.shoplab.domain.order.domain;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // 목록: 페이징이 있으므로 items를 fetch join하지 않는다 (batch fetch로 해결)
    Page<Order> findByMemberIdOrderByOrderedAtDesc(Long memberId, Pageable pageable);

    // 상세: 한 건이라 items를 함께 가져온다
    @EntityGraph(attributePaths = "items")
    Optional<Order> findWithItemsByOrderNumberAndMemberId(String orderNumber, Long memberId);

    // 주문 행 잠금 (결제 승인·취소·자동 취소가 동시에 같은 주문을 건드리지 않게)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
    @Query("select o from Order o where o.id = :id")
    Optional<Order> findByIdForUpdate(@Param("id") Long id);

    // 결제 시간이 지난 결제 대기 주문 id
    @Query("select o.id from Order o where o.status = :status and o.orderedAt < :before order by o.id")
    List<Long> findIdsByStatusAndOrderedAtBefore(@Param("status") OrderStatus status,
                                                 @Param("before") LocalDateTime before,
                                                 Pageable pageable);

}