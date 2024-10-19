package com.learnhive.lessonservice.domain.redis;

import com.learnhive.lessonservice.dto.CartDto;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.redis.core.RedisHash;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@RedisHash("cart") // Redis 데이터베이스에 저장될 엔티티로 지정
public class Cart {

    @Id
    private Long customerId;
    private List<Lesson> lessons = new ArrayList<>();
    private List<String> messages = new ArrayList<>();

    public Cart(Long customerId) {
        this.customerId = customerId;
    }

    public void addMessages(String message) {
        messages.add(message);
    }

    public void clearMessages() {
        messages.clear();
    }

    public CartDto toDto() {
        return new CartDto(
                this.customerId,
                this.lessons.stream()
                        .map(Lesson::toDto)
                        .collect(Collectors.toList()),
                new ArrayList<>(this.messages) // 메시지 리스트 복사
        );
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Lesson {
        private Long id;
        private Long coachId;
        private String title;
        private Integer price;
        private String description;
        private List<LessonSlot> lessonSlots = new ArrayList<>();

        public static Lesson fromDto(CartDto.Lesson lessonForm) {
            return Lesson.builder()
                    .id(lessonForm.id())
                    .coachId(lessonForm.coachId())
                    .title(lessonForm.title())
                    .price(lessonForm.price())
                    .lessonSlots(lessonForm.lessonSlots().stream()
                            .map(LessonSlot::fromDto)
                            .collect(Collectors.toList()))
                    .build();
        }

        public CartDto.Lesson toDto() {
            return new CartDto.Lesson(
                    this.id,
                    this.coachId,
                    this.title,
                    this.price,
                    this.description,
                    this.lessonSlots.stream()
                            .map(LessonSlot::toDto)
                            .collect(Collectors.toList())
            );
        }

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class LessonSlot {
            private Long id;
            private LocalDateTime startTime;
            private Integer quantity;

            public static LessonSlot fromDto(CartDto.Lesson.LessonSlot slotForm) {
                return LessonSlot.builder()
                        .id(slotForm.id())
                        .startTime(slotForm.startTime())
                        .quantity(slotForm.quantity())
                        .build();
            }

            public CartDto.Lesson.LessonSlot toDto() {
                return new CartDto.Lesson.LessonSlot(
                        this.id,
                        this.startTime,
                        this.quantity
                );
            }
        }
    }
}
