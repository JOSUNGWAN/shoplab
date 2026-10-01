package com.shoplab.domain.auth.api;

import com.shoplab.domain.auth.application.AuthService;
import com.shoplab.domain.auth.dto.AuthTokens;
import com.shoplab.domain.auth.dto.LoginRequest;
import com.shoplab.domain.auth.dto.LoginResponse;
import com.shoplab.domain.auth.dto.SignupRequest;
import com.shoplab.domain.auth.dto.SignupResponse;
import com.shoplab.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenCookieFactory cookieFactory;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(authService.signup(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        return withTokens(authService.login(request));
    }

    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<LoginResponse>> reissue(
            @CookieValue(name = RefreshTokenCookieFactory.COOKIE_NAME, required = false) String refreshToken) {
        return withTokens(authService.reissue(refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @CookieValue(name = RefreshTokenCookieFactory.COOKIE_NAME, required = false) String refreshToken) {
        authService.logout(refreshToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieFactory.expire().toString())
                .body(ApiResponse.ok(null));
    }

    private ResponseEntity<ApiResponse<LoginResponse>> withTokens(AuthTokens tokens) {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieFactory.create(tokens.refreshToken()).toString())
                .body(ApiResponse.ok(LoginResponse.of(tokens.accessToken(), tokens.expiresIn())));
    }
}