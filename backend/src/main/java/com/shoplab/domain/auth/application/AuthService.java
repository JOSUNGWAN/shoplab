package com.shoplab.domain.auth.application;

import com.shoplab.domain.auth.dto.*;
import com.shoplab.domain.auth.infrastructure.RefreshTokenRepository;
import com.shoplab.domain.member.domain.Member;
import com.shoplab.domain.member.domain.MemberRepository;
import com.shoplab.global.exception.BusinessException;
import com.shoplab.global.exception.ErrorCode;
import com.shoplab.global.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        Member member = Member.create(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.name(),
                request.phone()
        );

        return SignupResponse.from(memberRepository.save(member));
    }

    public AuthTokens login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_LOGIN));

        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_LOGIN);
        }

        return issueTokens(member);
    }

    public AuthTokens reissue(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        Long memberId = jwtProvider.parseRefreshToken(refreshToken)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));

        String savedToken = refreshTokenRepository.find(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));

        // 이미 교체된 옛 토큰이 다시 들어옴 = 탈취 의심 → 해당 회원 토큰 폐기
        if (!savedToken.equals(refreshToken)) {
            refreshTokenRepository.delete(memberId);
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));

        return issueTokens(member);
    }

    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }
        jwtProvider.parseRefreshToken(refreshToken).ifPresent(refreshTokenRepository::delete);
    }

    private AuthTokens issueTokens(Member member) {
        String accessToken = jwtProvider.createAccessToken(member.getId(), member.getRole());
        String refreshToken = jwtProvider.createRefreshToken(member.getId());
        refreshTokenRepository.save(member.getId(), refreshToken, jwtProvider.getRefreshTokenValidity());
        return new AuthTokens(accessToken, refreshToken, jwtProvider.getAccessTokenValiditySeconds());
    }
}