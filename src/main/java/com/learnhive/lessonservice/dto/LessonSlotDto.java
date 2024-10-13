package com.learnhive.lessonservice.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public record LessonSlotDto(
        @NotBlank(message = "시간 입력은 필수입니다.") LocalDateTime startTime,
        @NotBlank(message = "수량 입력은 필수입니다.") Integer quantity) {
}
