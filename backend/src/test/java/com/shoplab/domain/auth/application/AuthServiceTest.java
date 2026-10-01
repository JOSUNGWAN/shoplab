package com.shoplab.domain.auth.application;

import com.shoplab.domain.auth.dto.LoginRequest;
import com.shoplab.domain.auth.dto.SignupRequest;
import com.shoplab.domain.auth.dto.SignupResponse;
import com.shoplab.domain.auth.infrastructure.RefreshTokenRepository;
import com.shoplab.domain.member.domain.Member;
import com.shoplab.domain.member.domain.MemberRepository;
import com.shoplab.global.exception.BusinessException;
import com.shoplab.global.exception.ErrorCode;
import com.shoplab.global.security.JwtProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private final SignupRequest request =
            new SignupRequest("test@shoplab.com", "Test1234!", "홍길동", "010-1234-5678");

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Test
    @DisplayName("회원가입 성공 시 비밀번호를 암호화해서 저장한다")
    void signupSuccess() {
        given(memberRepository.existsByEmail(request.email())).willReturn(false);
        given(passwordEncoder.encode("Test1234!")).willReturn("encoded-password");
        given(memberRepository.save(any(Member.class))).willAnswer(invocation -> invocation.getArgument(0));

        SignupResponse response = authService.signup(request);

        assertThat(response.email()).isEqualTo("test@shoplab.com");
        assertThat(response.name()).isEqualTo("홍길동");
        verify(passwordEncoder).encode("Test1234!");
    }

    @Test
    @DisplayName("이미 가입된 이메일이면 DUPLICATE_EMAIL 예외가 발생한다")
    void signupDuplicateEmail() {
        given(memberRepository.existsByEmail(request.email())).willReturn(true);

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATE_EMAIL);

        verify(memberRepository, never()).save(any());
    }

    @Test
    @DisplayName("비밀번호가 틀리면 INVALID_LOGIN 예외가 발생한다")
    void loginWrongPassword() {
        Member member = Member.create("test@shoplab.com", "encoded", "홍길동", null);
        given(memberRepository.findByEmail("test@shoplab.com")).willReturn(Optional.of(member));
        given(passwordEncoder.matches("Wrong1234!", "encoded")).willReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("test@shoplab.com", "Wrong1234!")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_LOGIN);

        verify(jwtProvider, never()).createAccessToken(any(), any());
    }

    @Test
    @DisplayName("이미 교체된 Refresh Token으로 재발급하면 저장된 토큰을 폐기하고 예외가 발생한다")
    void reissueWithReusedToken() {
        given(jwtProvider.parseRefreshToken("old-token")).willReturn(Optional.of(1L));
        given(refreshTokenRepository.find(1L)).willReturn(Optional.of("latest-token"));

        assertThatThrownBy(() -> authService.reissue("old-token"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN);

        verify(refreshTokenRepository).delete(1L);
    }
}