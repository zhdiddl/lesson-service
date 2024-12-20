package com.learnhive.lessonservice.domain.lesson;

import com.learnhive.lessonservice.domain.AuditingFields;
import com.learnhive.lessonservice.domain.user.UserAccount;
import com.learnhive.lessonservice.exception.CustomException;
import com.learnhive.lessonservice.exception.ExceptionCode;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
@Entity
public class LessonSlot extends AuditingFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(updatable = false) // 한 번 설정 후 변경 불가
    private UserAccount coach;

    @ManyToOne(optional = false)
    @JoinColumn(updatable = false) // 한 번 설정 후 변경 불가
    private Lesson lesson;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private Integer quantity;


    // 시간대 변경 메소드
    public void updateStartTime(LocalDateTime newStartTime) {
        if (this.lesson.getLessonStatus() == LessonStatus.ACTIVE) {
            throw new CustomException(ExceptionCode.FORBIDDEN_UPDATE_ACTIVE_LESSON);
        }
        if (Objects.isNull(newStartTime) || newStartTime.isBefore(LocalDateTime.now())) {
            throw new CustomException(ExceptionCode.INVALID_TIME_REQUEST);
        }
        if (!Objects.equals(newStartTime, this.startTime)) {
            this.startTime = newStartTime;
        }
    }

    // 재고 변경 메소드
    public void updateQuantity(Integer newQuantity) {
        if (this.lesson.getLessonStatus() == LessonStatus.ACTIVE) {
            throw new CustomException(ExceptionCode.FORBIDDEN_UPDATE_ACTIVE_LESSON);
        }
        if (Objects.isNull(newQuantity) || newQuantity <= 0) {
            throw new CustomException(ExceptionCode.INVALID_QUANTITY_REQUEST);
        }
        if (!Objects.equals(newQuantity, this.quantity)) {
            this.quantity = newQuantity;
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LessonSlot that)) return false;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

}
