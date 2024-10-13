package com.learnhive.lessonservice.domain.lesson;

import com.learnhive.lessonservice.domain.AuditingFields;
import com.learnhive.lessonservice.domain.user.UserAccount;
import com.learnhive.lessonservice.exception.CustomException;
import com.learnhive.lessonservice.exception.ExceptionCode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
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
    @OrderBy("startTime DESC")
    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<LessonSlot> lessonSlots = new HashSet<>(); // 초기화된 후 참조값을 불변으로 유지하기 위해 dto 사용


    protected Lesson() {}

    private Lesson(UserAccount coach, String title, Integer price, String description, LessonStatus lessonStatus) {
        this.coach = coach;
        this.title = title;
        this.price = price;
        this.description = description;
        this.lessonStatus = lessonStatus;
    }

    public static Lesson of(UserAccount coach, String title, Integer price, String description, LessonStatus status) {
        return new Lesson(coach, title, price, description, status);
    }


    public void updateTitle(String newTitle) {
        if (newTitle.isEmpty()) {
            throw new CustomException(ExceptionCode.INVALID_INPUT_REQUEST);
        }
        if (!newTitle.equals(this.title)) {
            this.title = newTitle;
        }
    }

    public void updatePrice(Integer newPrice) {
        if (Objects.isNull(newPrice) || newPrice < 0) {
            throw new CustomException(ExceptionCode.INVALID_PRICE_REQUEST);
        }
        if (!newPrice.equals(this.price)) {
            this.price = newPrice;
        }
    }

    public void updateDescription(String newDescription) {
        if (Objects.isNull(newDescription)) {
            throw new CustomException(ExceptionCode.INVALID_INPUT_REQUEST);
        }
        if (!newDescription.equals(this.description)) {
            this.description = newDescription;
        }
    }

    public void updateStatus(LessonStatus newStatus) {
        if (Objects.isNull(newStatus)) {
            throw new CustomException(ExceptionCode.INVALID_INPUT_REQUEST);
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
