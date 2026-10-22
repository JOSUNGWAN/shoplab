package com.shoplab.domain.timedeal.domain;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

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

    // 3. 조건부 UPDATE: 남아 있을 때만 1 감소, 성공하면 1 반환
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update TimeDeal t set t.remainingQuantity = t.remainingQuantity - 1 "
            + "where t.id = :id and t.remainingQuantity > 0")
    int decreaseIfAvailable(@Param("id") Long id);
}