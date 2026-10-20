package com.shoplab.global.infra.toss;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "toss.payments")
public record TossPaymentsProperties(String baseUrl, String secretKey) {
}