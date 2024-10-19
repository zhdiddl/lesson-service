package com.learnhive.lessonservice.domain.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.learnhive.lessonservice.domain.AuditingFields;
import com.learnhive.lessonservice.exception.CustomException;
import com.learnhive.lessonservice.exception.ExceptionCode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class UserAccount extends AuditingFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    private UserRole userRole;

    @JsonIgnore
    private String emailVerificationCode;

    @JsonIgnore
    private LocalDateTime emailVerificationCodeExpiry;

    private boolean emailVerified = false;

    @UpdateTimestamp // 엔티티가 수정될 때 자동 업데이트
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime lastLogin;

    private boolean isDeleted = false;

    // 이름 변경 메소드
    public void changeUserName(String newUsername) {
        if (newUsername.isEmpty()) {
            throw new CustomException(ExceptionCode.UPDATE_FAIL_DUE_TO_INVALID_INPUT);
        }
        if (!Objects.equals(username, newUsername)) {
            this.username = newUsername;
        }
    }

    // 비밀번호 변경 메소드
    public void changePassword(String newPassword) {
        if (newPassword.isEmpty()) {
        throw new CustomException(ExceptionCode.UPDATE_FAIL_DUE_TO_INVALID_INPUT);
    }
        if (!Objects.equals(userPassword, newPassword)) {
            this.userPassword = newPassword;
        }
    }

    // 이메일 변경 메소드
    public void changeEmail(String newEmail) {
        if (newEmail.isEmpty()) {
            throw new CustomException(ExceptionCode.UPDATE_FAIL_DUE_TO_INVALID_INPUT);
        }
        if (!Objects.equals(email, newEmail)) {
            this.email = newEmail;
        }
    }

    public void changeEmailVerificationCode(String newEmailVerificationCode) {
        if (newEmailVerificationCode.isEmpty()) {
            throw new CustomException(ExceptionCode.UPDATE_FAIL_DUE_TO_INVALID_INPUT);
        }
        if (!Objects.equals(emailVerificationCode, newEmailVerificationCode)) {
            this.emailVerificationCode = newEmailVerificationCode;
        }
    }

    public void changeEmailVerificationCodeExpiry(LocalDateTime newEmailVerificationCodeExpiry) {
        if (newEmailVerificationCodeExpiry == null) {
            throw new CustomException(ExceptionCode.UPDATE_FAIL_DUE_TO_INVALID_INPUT);
        }
        if (!Objects.equals(emailVerificationCodeExpiry, newEmailVerificationCodeExpiry)) {
            this.emailVerificationCodeExpiry = newEmailVerificationCodeExpiry;
        }
    }

    // 이메일 인증 완료 처리 메소드
    public void verifyEmail() {
        if (!isEmailVerified()) {
            this.emailVerified = true;
        }
    }

    // 삭제 처리 메소드
    public void softDeleteAccount() {
        if (isDeleted) {
            throw new CustomException(ExceptionCode.USER_DELETED);
        }
        isDeleted = true;
    }

}
