package com.learnhive.lessonservice.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ExceptionCode {

    UNAUTHORIZED_USER(HttpStatus.UNAUTHORIZED, "사용자가 인증되지 않았습니다."),

    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "해당 이메일은 이미 사용 중입니다."),
    USERNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "해당 사용자명은 이미 사용 중입니다."),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 사용자를 찾을 수 없습니다."),

    FORBIDDEN_ACTION(HttpStatus.FORBIDDEN, "요청하신 작업을 수행할 권한이 없습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String message;
}
