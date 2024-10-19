package com.learnhive.lessonservice.domain.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

@RequiredArgsConstructor
@Getter
public enum UserRole implements GrantedAuthority {
    COACH("ROLE_COACH"),
    CUSTOMER("ROLE_CUSTOMER");

    private final String roleName;

    @Override
    public String getAuthority() {
        return this.roleName; // Spring Security 가 권한으로 사용할 수 있는 ROLE_을 붙인 값 반환
    }
}
