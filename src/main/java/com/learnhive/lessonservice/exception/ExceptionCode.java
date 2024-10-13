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
    LESSON_SLOT_ALREADY_EXISTS(HttpStatus.CONFLICT, "해당 슬롯 시간은 이미 사용 중입니다."),

    CANNOT_DELETE_PURCHASED_LESSON(HttpStatus.CONFLICT, "이미 구매자가 있는 레슨은 삭제할 수 없습니다."),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 사용자를 찾을 수 없습니다."),
    LESSON_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 레슨을 찾을 수 없습니다."),
    LESSON_SLOT_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 레슨 슬롯을 찾을 수 없습니다."),
    CUSTOMER_BALANCE_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 사용자 예치금을 조회할 수 없습니다."),

    FORBIDDEN_ACTION(HttpStatus.FORBIDDEN, "요청하신 작업을 수행할 권한이 없습니다."),
    FORBIDDEN_DELETE_ACTIVE_LESSON(HttpStatus.FORBIDDEN, "활성화된 레슨은 삭제가 불가합니다."),
    FORBIDDEN_UPDATE_ACTIVE_LESSON(HttpStatus.FORBIDDEN, "활성화된 레슨은 수정이 불가합니다."),

    INVALID_INPUT_REQUEST(HttpStatus.BAD_REQUEST, "입력한 값이 유효한지 다시 확인해 주세요."),
    INVALID_PRICE_REQUEST(HttpStatus.BAD_REQUEST, "0보다 작은 금액은 입력할 수 없습니다."),
    INVALID_TIME_REQUEST(HttpStatus.BAD_REQUEST, "과거 시간은 입력할 수 없습니다."),
    INVALID_QUANTITY_REQUEST(HttpStatus.BAD_REQUEST, "1보다 작은 재고는 입력할 수 없습니다."),
    CART_SAVE_FAIL(HttpStatus.BAD_REQUEST, "장바구니 저장에 실패했습니다."),
    INVALID_LESSON_SLOT(HttpStatus.BAD_REQUEST, "해당 슬롯을 선택할 수 없습니다."),
    NOT_ENOUGH_LESSON_SLOT_QUANTITY(HttpStatus.BAD_REQUEST, "해당 슬롯을 선택할 수 없습니다."),
    CART_UPDATE_REQUIRED(HttpStatus.BAD_REQUEST, "장바구니를 새로고침한 후 주문을 진행해 해주세요."),
    INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "결제에 필요한 예치금이 부족합니다."),


    EMAIL_VERIFICATION_EXPIRED(HttpStatus.BAD_REQUEST, "이메일 인증 코드가 만료되었습니다."),
    EMAIL_VERIFICATION_FAILED(HttpStatus.UNAUTHORIZED, "이메일 인증에 실패했습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String message;
}
