package com.learnhive.lessonservice.dto;

import java.time.LocalDateTime;
import java.util.List;

public record CartDto(
        Long customerId,
        List<Lesson> lessons,
        List<String> messages
) {
    public record Lesson(
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
}
