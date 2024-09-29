package com.learnhive.lessonservice.exception;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class ExceptionController {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ExceptionResponse> handleCustomException(CustomException e) {
        ExceptionCode exceptionCode = e.getExceptionCode();
        return ResponseEntity
                .status(exceptionCode.getHttpStatus())
                .body(new ExceptionResponse(exceptionCode.getMessage(), exceptionCode));
    }

    @AllArgsConstructor
    public static class ExceptionResponse {
        private String message;
        private ExceptionCode exceptionCode;
    }

}
