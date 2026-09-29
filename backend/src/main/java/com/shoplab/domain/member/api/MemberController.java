package com.shoplab.domain.member.api;

import com.shoplab.domain.member.application.MemberService;
import com.shoplab.domain.member.dto.MemberResponse;
import com.shoplab.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/me")
    public ApiResponse<MemberResponse> getMe(@AuthenticationPrincipal Long memberId) {
        return ApiResponse.ok(memberService.getMe(memberId));
    }
}