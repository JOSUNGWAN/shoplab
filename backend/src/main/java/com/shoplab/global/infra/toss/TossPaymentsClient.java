package com.shoplab.global.infra.toss;

import com.shoplab.global.exception.BusinessException;
import com.shoplab.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class TossPaymentsClient {

    private final RestClient restClient;

    public TossPaymentsClient(RestClient.Builder builder, TossPaymentsProperties properties) {
        // Basic 인증: base64("시크릿키:")  ← 끝의 콜론 필수
        String credentials = Base64.getEncoder()
                .encodeToString((properties.secretKey() + ":").getBytes(StandardCharsets.UTF_8));

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(3));
        requestFactory.setReadTimeout(Duration.ofSeconds(30));

        this.restClient = builder
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + credentials)
                .build();
    }

    /** 결제 승인 */
    public TossPaymentResponse confirm(String paymentKey, String orderId, long amount) {
        try {
            return restClient.post()
                    .uri("/v1/payments/confirm")
                    .header("Idempotency-Key", "confirm-" + paymentKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("paymentKey", paymentKey, "orderId", orderId, "amount", amount))
                    .retrieve()
                    .body(TossPaymentResponse.class);
        } catch (RestClientResponseException e) {
            throw toBusinessException(e, ErrorCode.PAYMENT_FAILED);
        } catch (ResourceAccessException e) {
            // 응답 지연: 실제로는 승인됐을 수 있으니 주문번호로 조회해서 확인
            log.warn("Toss confirm timeout. orderId={}", orderId, e);
            return findByOrderId(orderId)
                    .filter(payment -> "DONE".equals(payment.status()))
                    .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_FAILED,
                            "결제 승인 응답이 지연되었습니다. 잠시 후 주문 내역을 확인해 주세요."));
        }
    }

    /** 주문번호로 결제 조회 */
    public Optional<TossPaymentResponse> findByOrderId(String orderId) {
        try {
            return Optional.ofNullable(restClient.get()
                    .uri("/v1/payments/orders/{orderId}", orderId)
                    .retrieve()
                    .body(TossPaymentResponse.class));
        } catch (RestClientException e) {
            log.warn("Toss payment lookup failed. orderId={}", orderId, e);
            return Optional.empty();
        }
    }

    /** 결제 취소(전액) */
    public void cancel(String paymentKey, String reason) {
        try {
            restClient.post()
                    .uri("/v1/payments/{paymentKey}/cancel", paymentKey)
                    .header("Idempotency-Key", "cancel-" + paymentKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("cancelReason", reason))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw toBusinessException(e, ErrorCode.PAYMENT_CANCEL_FAILED);
        }
    }

    /** 보상 취소: 실패해도 예외를 던지지 않고 기록만 (원래 예외를 덮지 않기 위해) */
    public void cancelQuietly(String paymentKey, String reason) {
        try {
            cancel(paymentKey, reason);
        } catch (Exception e) {
            log.error("[수동 처리 필요] 보상 취소 실패. paymentKey={}", paymentKey, e);
        }
    }

    private BusinessException toBusinessException(RestClientResponseException e, ErrorCode errorCode) {
        TossErrorResponse error = null;
        try {
            error = e.getResponseBodyAs(TossErrorResponse.class);
        } catch (Exception ignored) {
            // 응답 본문을 읽지 못하면 기본 메시지 사용
        }
        String message = (error != null && error.message() != null) ? error.message() : errorCode.getMessage();
        log.warn("Toss API error. status={}, code={}, message={}",
                e.getStatusCode(), error != null ? error.code() : null, message);
        return new BusinessException(errorCode, message);
    }
}