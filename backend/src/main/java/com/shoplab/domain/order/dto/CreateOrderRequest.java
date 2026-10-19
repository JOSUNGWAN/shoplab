package com.shoplab.domain.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateOrderRequest(
        @NotEmpty(message = "주문할 상품이 없습니다.")
        @Size(max = 50, message = "한 번에 최대 50종까지 주문할 수 있습니다.")
        List<@Valid OrderItemRequest> items,

        @NotBlank(message = "받는 분 이름을 입력해 주세요.")
        @Size(max = 20) String recipient,

        @NotBlank(message = "연락처를 입력해 주세요.")
        @Pattern(regexp = "^01[0-9]-?\\d{3,4}-?\\d{4}$", message = "연락처 형식이 올바르지 않습니다.")
        String phone,

        @NotBlank(message = "우편번호를 입력해 주세요.")
        @Pattern(regexp = "^\\d{5}$", message = "우편번호는 5자리 숫자입니다.")
        String zipcode,

        @NotBlank(message = "주소를 입력해 주세요.")
        @Size(max = 200) String address1,

        @Size(max = 200) String address2
) {
}