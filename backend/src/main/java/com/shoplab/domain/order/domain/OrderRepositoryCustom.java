package com.shoplab.domain.order.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderRepositoryCustom {

    Page<Order> searchForAdmin(AdminOrderSearchCondition condition, Pageable pageable);
}