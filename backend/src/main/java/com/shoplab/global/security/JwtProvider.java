package com.shoplab.global.security;

import com.shoplab.domain.member.domain.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Component
public class JwtProvider {

    private static final String TYPE = "type";
    private static final String ACCESS = "access";
    private static final String REFRESH = "refresh";

    private final SecretKey key;
    private final Duration accessTokenValidity;
    private final Duration refreshTokenValidity;

    public JwtProvider(JwtProperties properties) {
        this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
        this.accessTokenValidity = properties.accessTokenExpiration();
        this.refreshTokenValidity = properties.refreshTokenExpiration();
    }

    public String createAccessToken(Long memberId, Role role) {
        return baseBuilder(memberId, ACCESS, accessTokenValidity)
                .claim("role", role.name())
                .compact();
    }

    public String createRefreshToken(Long memberId) {
        return baseBuilder(memberId, REFRESH, refreshTokenValidity).compact();
    }

    /** 서명이 틀리거나 만료되면 JwtException이 발생한다. */
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isAccessToken(Claims claims) {
        return ACCESS.equals(claims.get(TYPE, String.class));
    }

    /** 유효한 Refresh Token이면 회원 ID, 아니면 empty */
    public Optional<Long> parseRefreshToken(String token) {
        try {
            Claims claims = parseClaims(token);
            if (!REFRESH.equals(claims.get(TYPE, String.class))) {
                return Optional.empty();
            }
            return Optional.of(Long.valueOf(claims.getSubject()));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public long getAccessTokenValiditySeconds() {
        return accessTokenValidity.toSeconds();
    }

    public Duration getRefreshTokenValidity() {
        return refreshTokenValidity;
    }

    private JwtBuilder baseBuilder(Long memberId, String type, Duration validity) {
        Date now = new Date();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(String.valueOf(memberId))
                .claim(TYPE, type)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + validity.toMillis()))
                .signWith(key);
    }
}