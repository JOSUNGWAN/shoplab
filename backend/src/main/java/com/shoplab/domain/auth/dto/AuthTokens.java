package com.shoplab.domain.auth.dto;

public record AuthTokens(String accessToken, String refreshToken, long expiresIn) {
}