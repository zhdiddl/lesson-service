package com.learnhive.lessonservice.repository;

import com.learnhive.lessonservice.domain.LessonSlot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonSlotRepository extends JpaRepository<LessonSlot, Long> {
}
