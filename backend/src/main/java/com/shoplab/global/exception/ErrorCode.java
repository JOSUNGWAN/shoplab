package com.shoplab.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    INVALID_LOGIN(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 가입된 이메일입니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "로그인이 만료되었습니다. 다시 로그인해 주세요."),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."),
    INVALID_PRICE_RANGE(HttpStatus.BAD_REQUEST, "최소 가격이 최대 가격보다 클 수 없습니다."),
    PRODUCT_NOT_ON_SALE(HttpStatus.BAD_REQUEST, "판매 중인 상품이 아닙니다."),
    OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "재고가 부족합니다."),
    CART_QUANTITY_EXCEEDED(HttpStatus.BAD_REQUEST, "한 상품은 최대 99개까지 담을 수 있습니다."),
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "장바구니 상품을 찾을 수 없습니다."),
    DUPLICATE_REQUEST(HttpStatus.CONFLICT, "요청이 중복되었습니다. 잠시 후 다시 시도해 주세요."),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."),
    INVALID_ORDER_STATUS(HttpStatus.CONFLICT, "현재 주문 상태에서는 처리할 수 없습니다."),
    ORDER_CANNOT_BE_CANCELED(HttpStatus.CONFLICT, "배송 준비가 시작된 주문은 취소할 수 없습니다."),
    ORDER_ALREADY_CANCELED(HttpStatus.CONFLICT, "이미 취소된 주문입니다."),
    ORDER_BUSY(HttpStatus.CONFLICT, "주문이 몰려 처리하지 못했습니다. 잠시 후 다시 시도해 주세요."),
    AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "결제 금액이 주문 금액과 일치하지 않습니다."),
    ORDER_EXPIRED(HttpStatus.CONFLICT, "결제 가능 시간이 지났습니다. 다시 주문해 주세요."),
    PAYMENT_FAILED(HttpStatus.BAD_REQUEST, "결제 승인에 실패했습니다."),
    PAYMENT_CANCEL_FAILED(HttpStatus.BAD_GATEWAY, "결제 취소에 실패했습니다. 잠시 후 다시 시도해 주세요."),
    TIME_DEAL_NOT_FOUND(HttpStatus.NOT_FOUND, "타임딜을 찾을 수 없습니다."),
    TIME_DEAL_SOLD_OUT(HttpStatus.CONFLICT, "타임딜 상품이 모두 판매되었습니다."),
    TIME_DEAL_NOT_OPEN(HttpStatus.CONFLICT, "타임딜 진행 시간이 아닙니다."),
    TIME_DEAL_ALREADY_PURCHASED(HttpStatus.CONFLICT, "타임딜 상품은 1인 1개만 구매할 수 있습니다.");

    private final HttpStatus status;
    private final String message;
}