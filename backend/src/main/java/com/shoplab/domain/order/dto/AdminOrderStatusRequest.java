package com.shoplab.domain.order.dto;

import com.shoplab.domain.order.domain.OrderStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AdminOrderStatusRequest(
        @NotEmpty(message = "변경할 주문을 선택해 주세요.")
        @Size(max = 100, message = "한 번에 최대 100건까지 변경할 수 있습니다.")
        List<@NotBlank String> orderNumbers,

        @NotNull(message = "변경할 상태를 선택해 주세요.")
        OrderStatus status
) {
}