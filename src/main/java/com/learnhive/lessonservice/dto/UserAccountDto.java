package com.learnhive.lessonservice.dto;

import com.learnhive.lessonservice.domain.user.UserRole;
import jakarta.validation.constraints.*;

public record UserAccountDto (
    @NotBlank(message = "사용자 이름은 필수입니다.")
    String username,

    @NotBlank(message = "비밀번호는 필수입니다.") @Size(min = 8, max = 32, message = "비밀번호는 8자 이상 32자 이하여야 합니다.")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,32}$",
            message = "비밀번호는 대소문자, 숫자, 특수문자를 포함해야 합니다."
    )
    String userPassword,

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "유효하지 않은 이메일 형식입니다.")
    String email,

    @NotEmpty(message = "COACH 혹은 CUSTOMER 중에 선택하세요. 권한은 설정 후 변경이 불가합니다.")
    UserRole userRole){
}
