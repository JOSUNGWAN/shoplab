package com.shoplab.global.config;

import com.shoplab.domain.member.domain.Member;
import com.shoplab.domain.member.domain.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("local")
@Order(3)
@RequiredArgsConstructor
public class LocalAdminInitializer implements ApplicationRunner {

    private static final String ADMIN_EMAIL = "admin@shoplab.com";
    private static final String ADMIN_PASSWORD = "Admin1234!";   // 로컬 개발 전용

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (memberRepository.existsByEmail(ADMIN_EMAIL)) {
            return;
        }
        memberRepository.save(Member.createAdmin(ADMIN_EMAIL, passwordEncoder.encode(ADMIN_PASSWORD), "관리자"));
        log.info("로컬 관리자 계정 생성 완료: {}", ADMIN_EMAIL);
    }
}