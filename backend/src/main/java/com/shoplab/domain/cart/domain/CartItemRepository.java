package com.shoplab.domain.cart.domain;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    @EntityGraph(attributePaths = "product")
    List<CartItem> findAllByMemberIdOrderByCreatedAtDesc(Long memberId);

    Optional<CartItem> findByMemberIdAndProductId(Long memberId, Long productId);

    // 본인 것만 조회 → 남의 항목 ID를 넣으면 "없음"
    @EntityGraph(attributePaths = "product")
    Optional<CartItem> findByIdAndMemberId(Long id, Long memberId);

    long countByMemberId(Long memberId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from CartItem c where c.id in :ids and c.member.id = :memberId")
    int deleteByIdsAndMemberId(@Param("ids") Collection<Long> ids, @Param("memberId") Long memberId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from CartItem c where c.member.id = :memberId and c.product.id in :productIds")
    int deleteByMemberIdAndProductIds(@Param("memberId") Long memberId,
                                      @Param("productIds") Collection<Long> productIds);
}