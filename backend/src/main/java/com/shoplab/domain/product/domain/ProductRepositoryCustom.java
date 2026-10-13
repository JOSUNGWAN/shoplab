package com.shoplab.domain.product.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepositoryCustom {

    Page<Product> search(ProductSearchCondition condition, Pageable pageable);
}