package com.shoplab.domain.timedeal.domain;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TimeDealRepository extends JpaRepository<TimeDeal, Long> {

    @Query("select t.remainingQuantity from TimeDeal t where t.id = :id")
    int findRemainingQuantity(@Param("id") Long id);

    // 0. 락 없음: 계산한 값을 그대로 덮어쓰기 (갱신 손실 재현용)
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update TimeDeal t set t.remainingQuantity = :remaining where t.id = :id")
    int overwriteRemaining(@Param("id") Long id, @Param("remaining") int remaining);

    // 1. 비관적 락
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
    @Query("select t from TimeDeal t where t.id = :id")
    Optional<TimeDeal> findByIdForUpdate(@Param("id") Long id);

    // 3. 조건부 UPDATE (clearAutomatically 제거)
    @Modifying(flushAutomatically = true)
    @Query("update TimeDeal t set t.remainingQuantity = t.remainingQuantity - 1 "
            + "where t.id = :id and t.remainingQuantity > 0")
    int decreaseIfAvailable(@Param("id") Long id);

    // 취소 시 수량 반환 (총수량을 넘지 않게)
    @Modifying(flushAutomatically = true)
    @Query("update TimeDeal t set t.remainingQuantity = t.remainingQuantity + 1 "
            + "where t.id = :id and t.remainingQuantity < t.totalQuantity")
    int increaseIfBelowTotal(@Param("id") Long id);

    @EntityGraph(attributePaths = "product")
    Optional<TimeDeal> findWithProductById(Long id);

    @EntityGraph(attributePaths = "product")
    List<TimeDeal> findByEndAtAfterOrderByStartAtAsc(LocalDateTime now);
}