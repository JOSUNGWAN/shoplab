package com.shoplab.domain.order.dto;

import java.util.List;

public record AdminBulkStatusResponse(int successCount, List<Failure> failures) {

    public record Failure(String orderNumber, String reason) {
    }
}