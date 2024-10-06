package com.learnhive.lessonservice.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
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

    @Setter
    @ManyToOne(optional = false)
    @JoinColumn
    private UserAccount coach;

    @Setter
    @Column(nullable = false, unique = true)
    private String title;

    @Setter
    @Column(nullable = false)
    private Integer price;

    @Setter
    private String description;

    @ToString.Exclude
    @OrderBy("startTime DESC")
    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    private final Set<LessonSlot> lessonSlot = new HashSet<>(); // 필드가 한 번 초기화되면 참조값은 불변


    protected Lesson() {}

    private Lesson(UserAccount coach, String title, Integer price, String description) {
        this.coach = coach;
        this.title = title;
        this.price = price;
        this.description = description;
    }

    public static Lesson of(UserAccount coach, String title, Integer price, String description) {
        return new Lesson(coach, title, price, description);
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
