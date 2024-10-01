package com.learnhive.lessonservice.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.stream.Collectors;

@ControllerAdvice
public class ExceptionController {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ExceptionResponse> handleCustomException(CustomException e) {
        ExceptionCode exceptionCode = e.getExceptionCode();
        return ResponseEntity
                .status(exceptionCode.getHttpStatus())
                .body(new ExceptionResponse(exceptionCode.getMessage(), exceptionCode));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String errorMessages = e.getBindingResult().getAllErrors().stream()
                .map(ObjectError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ResponseEntity
                .badRequest().body(errorMessages);
    }

    @AllArgsConstructor
    @Getter
    public static class ExceptionResponse {
        private String message;
        private ExceptionCode exceptionCode;
    }

}
