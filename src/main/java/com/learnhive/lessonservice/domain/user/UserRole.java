package com.learnhive.lessonservice.domain.user;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

@Getter
public enum UserRole implements GrantedAuthority {
    COACH("ROLE_COACH"),
    CLIENT("ROLE_CLIENT");

    private final String roleName;

    UserRole(String roleName) {
        this.roleName = roleName;
    }

    @Override
    public String getAuthority() {
        return this.roleName; // Spring Security 가 권한으로 사용할 수 있는 ROLE_을 붙인 값 반환
    }
}
