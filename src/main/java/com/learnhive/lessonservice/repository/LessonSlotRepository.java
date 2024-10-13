package com.learnhive.lessonservice.repository;

import com.learnhive.lessonservice.domain.lesson.LessonSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LessonSlotRepository extends JpaRepository<LessonSlot, Long> {
    Optional<LessonSlot> findByIdAndLessonIdAndCoachId(Long Id, Long lessonId, Long coachId);

}
