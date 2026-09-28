package com.shoplab.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest(

        @NotBlank(message = "이메일을 입력해 주세요.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email,

        @NotBlank(message = "비밀번호를 입력해 주세요.")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*]).{8,20}$",
                message = "비밀번호는 영문, 숫자, 특수문자(!@#$%^&*)를 포함해 8~20자여야 합니다.")
        String password,

        @NotBlank(message = "이름을 입력해 주세요.")
        @Size(min = 2, max = 20, message = "이름은 2~20자여야 합니다.")
        String name,

        @Pattern(regexp = "^01[0-9]-?\\d{3,4}-?\\d{4}$", message = "휴대폰 번호 형식이 올바르지 않습니다.")
        String phone
) {
}