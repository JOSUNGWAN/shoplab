package com.shoplab.domain.dashboard.application;

import com.shoplab.domain.dashboard.dto.DailySalesResponse;
import com.shoplab.domain.dashboard.dto.DashboardRequest;
import com.shoplab.domain.dashboard.infrastructure.DashboardQueryRepository;
import com.shoplab.global.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceTest {

    @Mock
    DashboardQueryRepository dashboardQueryRepository;

    @InjectMocks
    AdminDashboardService adminDashboardService;

    @Test
    @DisplayName("주문이 없는 날은 매출 0으로 채워 기간의 모든 날짜를 반환한다")
    void fillsEmptyDays() {
        LocalDate from = LocalDate.of(2026, 11, 1);
        LocalDate to = LocalDate.of(2026, 11, 5);
        given(dashboardQueryRepository.findDailySales(any(), any())).willReturn(List.of(
                new DailySalesResponse(LocalDate.of(2026, 11, 2), 3, 45_000),
                new DailySalesResponse(LocalDate.of(2026, 11, 4), 1, 12_000)));

        List<DailySalesResponse> result = adminDashboardService.getDailySales(new DashboardRequest(from, to, null));

        assertThat(result).hasSize(5);
        assertThat(result).extracting(DailySalesResponse::date)
                .containsExactly(from, from.plusDays(1), from.plusDays(2), from.plusDays(3), to);
        assertThat(result).extracting(DailySalesResponse::salesAmount)
                .containsExactly(0L, 45_000L, 0L, 12_000L, 0L);
    }

    @Test
    @DisplayName("조회 기간이 92일을 넘으면 예외")
    void rejectsTooLongPeriod() {
        DashboardRequest request = new DashboardRequest(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), null);

        assertThatThrownBy(() -> adminDashboardService.getDailySales(request))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("시작일이 종료일보다 늦으면 예외")
    void rejectsReversedPeriod() {
        DashboardRequest request = new DashboardRequest(LocalDate.of(2026, 11, 5), LocalDate.of(2026, 11, 1), null);

        assertThatThrownBy(() -> adminDashboardService.getTopProducts(request))
                .isInstanceOf(BusinessException.class);
    }
}