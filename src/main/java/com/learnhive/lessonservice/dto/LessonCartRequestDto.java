package com.learnhive.lessonservice.dto;

import java.time.LocalDateTime;
import java.util.List;

public record LessonCartRequestDto(
        Long id,
        Long coachId,
        String title,
        Integer price,
        String description,
        List<LessonSlot> lessonSlots
) {
    public record LessonSlot(
            Long id,
            LocalDateTime startTime,
            Integer quantity
    ) {}
}
