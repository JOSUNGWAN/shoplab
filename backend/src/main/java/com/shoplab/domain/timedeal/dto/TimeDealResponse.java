package com.shoplab.domain.timedeal.dto;

import com.shoplab.domain.product.domain.Product;
import com.shoplab.domain.timedeal.domain.TimeDeal;

import java.time.LocalDateTime;

public record TimeDealResponse(
        Long id,
        Long productId,
        String productName,
        String thumbnailUrl,
        int originalPrice,
        int dealPrice,
        int discountRate,
        int totalQuantity,
        int remainingQuantity,
        LocalDateTime startAt,
        LocalDateTime endAt,
        String status   // UPCOMING, ONGOING, SOLD_OUT, ENDED
) {
    public static TimeDealResponse from(TimeDeal deal, LocalDateTime now) {
        Product product = deal.getProduct();
        int discountRate = (int) Math.round((1 - (double) deal.getDealPrice() / product.getPrice()) * 100);
        return new TimeDealResponse(
                deal.getId(), product.getId(), product.getName(), product.getThumbnailUrl(),
                product.getPrice(), deal.getDealPrice(), discountRate,
                deal.getTotalQuantity(), deal.getRemainingQuantity(),
                deal.getStartAt(), deal.getEndAt(), status(deal, now));
    }

    private static String status(TimeDeal deal, LocalDateTime now) {
        if (now.isBefore(deal.getStartAt())) return "UPCOMING";
        if (!now.isBefore(deal.getEndAt())) return "ENDED";
        if (deal.getRemainingQuantity() <= 0) return "SOLD_OUT";
        return "ONGOING";
    }
}