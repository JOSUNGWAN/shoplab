package com.shoplab.domain.auth.application;

import com.shoplab.domain.auth.dto.LoginRequest;
import com.shoplab.domain.auth.dto.LoginResponse;
import com.shoplab.domain.auth.dto.SignupRequest;
import com.shoplab.domain.auth.dto.SignupResponse;
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

    public LoginResponse login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_LOGIN));

        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_LOGIN);
        }

        String accessToken = jwtProvider.createAccessToken(member.getId(), member.getRole());
        return LoginResponse.of(accessToken, jwtProvider.getAccessTokenValiditySeconds());
    }
}