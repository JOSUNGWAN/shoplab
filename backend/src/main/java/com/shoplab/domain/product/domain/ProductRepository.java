package com.shoplab.domain.product.domain;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom {

    @EntityGraph(attributePaths = "category")
    Optional<Product> findWithCategoryById(Long id);

    // 주문용: 상품 행을 잠그고 조회 (id 순서로 잠가 데드락 방지)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
    @Query("select p from Product p where p.id in :ids order by p.id")
    List<Product> findAllByIdInForUpdate(@Param("ids") Collection<Long> ids);

    // 관리자 재고·상태 변경용: 주문과 같은 상품 행 락
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
    @Query("select p from Product p join fetch p.category where p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);
}