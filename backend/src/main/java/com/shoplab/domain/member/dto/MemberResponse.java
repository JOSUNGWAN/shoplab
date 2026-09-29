package com.shoplab.domain.member.dto;

import com.shoplab.domain.member.domain.Member;

public record MemberResponse(Long id, String email, String name, String phone, String role) {

    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(), member.getEmail(), member.getName(),
                member.getPhone(), member.getRole().name());
    }
}