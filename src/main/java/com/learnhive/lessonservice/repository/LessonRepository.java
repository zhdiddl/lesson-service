package com.learnhive.lessonservice.repository;

import com.learnhive.lessonservice.domain.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    Optional<Lesson> findByTitle(String title);
}
