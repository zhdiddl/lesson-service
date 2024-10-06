package com.learnhive.lessonservice.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class UserAccount extends AuditingFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @JsonIgnore
    @Column(nullable = false)
    private String userPassword;

    @JsonIgnore
    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Setter(AccessLevel.NONE) // 생성자로 설정 후 변경 불가
    private UserRole userRole;

    @JsonIgnore
    private String emailVerificationCode;

    @JsonIgnore
    private LocalDateTime emailVerificationCodeExpiry;

    private boolean emailVerified = false;

    @UpdateTimestamp
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime lastLogin;

    private boolean isDeleted = false;

    protected UserAccount() {}

    private UserAccount(String username, String password, String email, UserRole userRole) {
        this.username = username;
        this.userPassword = password;
        this.email = email;
        this.userRole = userRole;
    }

    public static UserAccount of(String username, String password, String email, UserRole userRole) {
        return new UserAccount(username, password, email, userRole);
    }

}
