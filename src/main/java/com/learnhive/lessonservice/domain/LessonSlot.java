package com.learnhive.lessonservice.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@ToString(callSuper = true)
@Entity
public class LessonSlot extends AuditingFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @ManyToOne(optional = false)
    private UserAccount coach;

    @ManyToOne(optional = false)
    private Lesson lesson;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private Integer quantity;


    protected LessonSlot() {}

    private LessonSlot(UserAccount coach, Lesson lesson, LocalDateTime startTime, Integer quantity) {
        this.coach = coach;
        this.lesson = lesson;
        this.startTime = startTime;
        this.quantity = quantity;
    }

    public static LessonSlot of(UserAccount coach, Lesson lesson, LocalDateTime startTime, Integer quantity) {
        return new LessonSlot(coach, lesson, startTime, quantity);
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
