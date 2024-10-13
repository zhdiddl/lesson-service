package com.learnhive.lessonservice.dto;

import com.learnhive.lessonservice.domain.lesson.LessonStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record LessonDto(
        @NotBlank(message = "레슨명은 필수입니다.") String title,
        @NotBlank(message = "레슨 가격은 필수입니다.") Integer price,
        String description,
        @NotEmpty(message = "활성화 여부를 설정해주세요.") LessonStatus status) {
}
