package com.learnhive.lessonservice.domain.redis;

import com.learnhive.lessonservice.dto.LessonCartRequestDto;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@RedisHash("cart")
public class Cart {

    @Id
    private Long customerId;
    private List<Lesson> lessons = new ArrayList<>();
    private List<String> messages = new ArrayList<>();

    public Cart(Long customerId) {
        this.customerId = customerId;
    }

    public void addMessage(String message) {
        messages.add(message);
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Lesson {
        private Long id;
        private Long coachId;
        private String title;
        private Integer price;
        private List<LessonSlot> lessonSlots = new ArrayList<>();

        // LessonCartRequestDto를 받아 Lesson 객체로 변환하는 메서드
        public static Lesson fromRequestForm(LessonCartRequestDto form) {
            return Lesson.builder()
                    .id(form.id())
                    .coachId(form.coachId())
                    .title(form.title())
                    .price(form.price())
                    .lessonSlots(form.lessonSlots().stream()
                            .map(LessonSlot::fromRequestForm)
                            .collect(Collectors.toList()))
                    .build();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LessonSlot {
        private Long id;
        private LocalDateTime startTime;
        private Integer quantity;

        // LessonCartRequestDto.LessonSlot를 받아 LessonSlot 객체로 변환하는 메서드
        public static LessonSlot fromRequestForm(LessonCartRequestDto.LessonSlot slotForm) {
            return LessonSlot.builder()
                    .id(slotForm.id())
                    .startTime(slotForm.startTime())
                    .quantity(slotForm.quantity())
                    .build();
        }
    }


}
