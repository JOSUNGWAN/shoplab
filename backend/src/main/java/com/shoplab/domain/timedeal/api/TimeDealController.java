package com.shoplab.domain.timedeal.api;

import com.shoplab.domain.order.dto.OrderDetailResponse;
import com.shoplab.domain.timedeal.application.TimeDealOrderService;
import com.shoplab.domain.timedeal.application.TimeDealQueryService;
import com.shoplab.domain.timedeal.dto.TimeDealOrderRequest;
import com.shoplab.domain.timedeal.dto.TimeDealResponse;
import com.shoplab.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/time-deals")
@RequiredArgsConstructor
public class TimeDealController {

    private final TimeDealQueryService timeDealQueryService;
    private final TimeDealOrderService timeDealOrderService;

    @GetMapping
    public ApiResponse<List<TimeDealResponse>> getTimeDeals() {
        return ApiResponse.ok(timeDealQueryService.getTimeDeals());
    }

    @GetMapping("/{timeDealId}")
    public ApiResponse<TimeDealResponse> getTimeDeal(@PathVariable Long timeDealId) {
        return ApiResponse.ok(timeDealQueryService.getTimeDeal(timeDealId));
    }

    @PostMapping("/{timeDealId}/orders")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> purchase(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long timeDealId,
            @Valid @RequestBody TimeDealOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(timeDealOrderService.purchase(memberId, timeDealId, request)));
    }
}