package com.shoplab.global.security;

import com.shoplab.domain.member.domain.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtProvider {

    private final SecretKey key;
    private final long accessTokenValidityMillis;

    public JwtProvider(JwtProperties properties) {
        this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
        this.accessTokenValidityMillis = properties.accessTokenExpiration().toMillis();
    }

    public String createAccessToken(Long memberId, Role role) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .claim("role", role.name())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessTokenValidityMillis))
                .signWith(key)
                .compact();
    }

    /** 서명이 틀리거나 만료되면 JwtException이 발생한다. */
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getAccessTokenValiditySeconds() {
        return accessTokenValidityMillis / 1000;
    }
}