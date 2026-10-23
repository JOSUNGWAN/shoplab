package com.shoplab.domain.timedeal.dto;

import com.shoplab.domain.order.domain.ShippingInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TimeDealOrderRequest(
        @NotBlank(message = "받는 분 이름을 입력해 주세요.") @Size(max = 20) String recipient,
        @NotBlank(message = "연락처를 입력해 주세요.")
        @Pattern(regexp = "^01[0-9]-?\\d{3,4}-?\\d{4}$", message = "연락처 형식이 올바르지 않습니다.")
        String phone,
        @NotBlank(message = "우편번호를 입력해 주세요.")
        @Pattern(regexp = "^\\d{5}$", message = "우편번호는 5자리 숫자입니다.")
        String zipcode,
        @NotBlank(message = "주소를 입력해 주세요.") @Size(max = 200) String address1,
        @Size(max = 200) String address2
) {
    public ShippingInfo toShippingInfo() {
        return new ShippingInfo(recipient, phone, zipcode, address1, address2);
    }
}