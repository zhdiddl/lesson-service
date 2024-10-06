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

    CANNOT_DELETE_PURCHASED_LESSON(HttpStatus.CONFLICT, "이미 구매자가 있는 레슨은 삭제할 수 없습니다."),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 사용자를 찾을 수 없습니다."),
    LESSON_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 레슨을 찾을 수 없습니다."),

    FORBIDDEN_ACTION(HttpStatus.FORBIDDEN, "요청하신 작업을 수행할 권한이 없습니다."),

    EMAIL_VERIFICATION_EXPIRED(HttpStatus.BAD_REQUEST, "이메일 인증 코드가 만료되었습니다."),
    EMAIL_VERIFICATION_FAILED(HttpStatus.UNAUTHORIZED, "이메일 인증에 실패했습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String message;
}
