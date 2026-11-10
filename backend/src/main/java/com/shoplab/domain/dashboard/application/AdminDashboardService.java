package com.shoplab.domain.dashboard.application;

import com.shoplab.domain.dashboard.dto.DailySalesResponse;
import com.shoplab.domain.dashboard.dto.DashboardRequest;
import com.shoplab.domain.dashboard.dto.DashboardSummaryResponse;
import com.shoplab.domain.dashboard.dto.TopProductResponse;
import com.shoplab.domain.dashboard.infrastructure.DashboardQueryRepository;
import com.shoplab.domain.dashboard.infrastructure.DashboardQueryRepository.ProductStockCount;
import com.shoplab.domain.dashboard.infrastructure.DashboardQueryRepository.SalesTotal;
import com.shoplab.domain.dashboard.infrastructure.DashboardQueryRepository.TopProductRow;
import com.shoplab.domain.order.domain.OrderStatus;
import com.shoplab.global.exception.BusinessException;
import com.shoplab.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardService {

    private static final int MAX_PERIOD_DAYS = 92;      // 약 3개월
    private static final int LOW_STOCK_THRESHOLD = 5;

    private final DashboardQueryRepository dashboardQueryRepository;

    public DashboardSummaryResponse getSummary() {
        LocalDate today = LocalDate.now();
        SalesTotal todayTotal = dashboardQueryRepository.findSalesTotal(
                today.atStartOfDay(), today.plusDays(1).atStartOfDay());
        SalesTotal yesterdayTotal = dashboardQueryRepository.findSalesTotal(
                today.minusDays(1).atStartOfDay(), today.atStartOfDay());
        Map<OrderStatus, Long> statusCounts = dashboardQueryRepository.countOrdersByStatus();
        ProductStockCount stock = dashboardQueryRepository.countProductStock(LOW_STOCK_THRESHOLD);

        return new DashboardSummaryResponse(
                todayTotal.salesAmount(),
                todayTotal.orderCount(),
                yesterdayTotal.salesAmount(),
                statusCounts.getOrDefault(OrderStatus.PAID, 0L),
                statusCounts.getOrDefault(OrderStatus.PREPARING, 0L),
                statusCounts.getOrDefault(OrderStatus.SHIPPING, 0L),
                stock.soldOut(),
                stock.lowStock(),
                LOW_STOCK_THRESHOLD);
    }

    /** 일별 매출: 주문이 없는 날도 0으로 채워 기간의 모든 날짜를 반환 */
    public List<DailySalesResponse> getDailySales(DashboardRequest request) {
        validatePeriod(request);
        Map<LocalDate, DailySalesResponse> salesByDate = dashboardQueryRepository
                .findDailySales(request.from().atStartOfDay(), request.to().plusDays(1).atStartOfDay())
                .stream()
                .collect(Collectors.toMap(DailySalesResponse::date, Function.identity()));

        return request.from().datesUntil(request.to().plusDays(1))
                .map(date -> salesByDate.getOrDefault(date, DailySalesResponse.empty(date)))
                .toList();
    }

    public List<TopProductResponse> getTopProducts(DashboardRequest request) {
        validatePeriod(request);
        List<TopProductRow> rows = dashboardQueryRepository.findTopProducts(
                request.from().atStartOfDay(), request.to().plusDays(1).atStartOfDay(), request.limit());

        return IntStream.range(0, rows.size())
                .mapToObj(i -> TopProductResponse.of(i + 1, rows.get(i)))
                .toList();
    }

    private void validatePeriod(DashboardRequest request) {
        if (request.from().isAfter(request.to())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "시작일이 종료일보다 늦을 수 없습니다.");
        }
        if (ChronoUnit.DAYS.between(request.from(), request.to()) + 1 > MAX_PERIOD_DAYS) {
            throw new BusinessException(ErrorCode.INVALID_INPUT,
                    "조회 기간은 최대 " + MAX_PERIOD_DAYS + "일입니다.");
        }
    }
}