package com.shoplab.domain.product.application;

import com.shoplab.domain.product.domain.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class ProductLockManager {

    private final ProductRepository productRepository;

    /** 재고를 바꾸기 전에 상품 행을 id 순서로 잠근다 (트랜잭션 안에서 호출) */
    public void lock(Collection<Long> productIds) {
        productRepository.findAllByIdInForUpdate(productIds);
    }
}