package com.learnhive.lessonservice.domain.lesson;

import com.learnhive.lessonservice.domain.AuditingFields;
import com.learnhive.lessonservice.domain.user.UserAccount;
import com.learnhive.lessonservice.exception.CustomException;
import com.learnhive.lessonservice.exception.ExceptionCode;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Entity
public class Lesson extends AuditingFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(updatable = false) // 한 번 설정 후 변경 불가
    private UserAccount coach;

    @Column(nullable = false, unique = true)
    private String title;

    @Column(nullable = false)
    private Integer price;

    private String description;

    private LessonStatus lessonStatus = LessonStatus.INACTIVE;

    @ToString.Exclude
    @OrderBy("startTime")
    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LessonSlot> lessonSlots = new ArrayList<>();


    // 제목 변경 메소드
    public void updateTitle(String newTitle) {
        if (newTitle.isEmpty()) {
            throw new CustomException(ExceptionCode.UPDATE_FAIL_DUE_TO_INVALID_INPUT);
        }
        if (!newTitle.equals(this.title)) {
            this.title = newTitle;
        }
    }

    // 가격 변경 메소드
    public void updatePrice(Integer newPrice) {
        if (Objects.isNull(newPrice) || newPrice < 0) {
            throw new CustomException(ExceptionCode.INVALID_PRICE_REQUEST);
        }
        if (!newPrice.equals(this.price)) {
            this.price = newPrice;
        }
    }

    // 설명 변경 메소드
    public void updateDescription(String newDescription) {
        if (Objects.isNull(newDescription)) {
            throw new CustomException(ExceptionCode.UPDATE_FAIL_DUE_TO_INVALID_INPUT);
        }
        if (!newDescription.equals(this.description)) {
            this.description = newDescription;
        }
    }

    // 활성화 상태 변경 메소드 (기본은 비활성화)
    public void updateStatus(LessonStatus newStatus) {
        if (Objects.isNull(newStatus)) {
            throw new CustomException(ExceptionCode.UPDATE_FAIL_DUE_TO_INVALID_INPUT);
        }
        if (!Objects.equals(newStatus, this.lessonStatus)) {
            this.lessonStatus = newStatus;
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Lesson lesson)) return false;
        return Objects.equals(getId(), lesson.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

}
