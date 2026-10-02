package com.shoplab.domain.product.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @EntityGraph(attributePaths = "category")
    Page<Product> findByStatusIn(Collection<ProductStatus> statuses, Pageable pageable);

    @EntityGraph(attributePaths = "category")
    Page<Product> findByCategoryIdInAndStatusIn(Collection<Long> categoryIds,
                                                Collection<ProductStatus> statuses,
                                                Pageable pageable);

    @EntityGraph(attributePaths = "category")
    Optional<Product> findWithCategoryById(Long id);
}