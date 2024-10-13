package com.learnhive.lessonservice.repository;

import com.learnhive.lessonservice.domain.lesson.Lesson;
import com.learnhive.lessonservice.domain.lesson.LessonStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LessonRepository extends JpaRepository<Lesson, Long>, LessonRepositoryCustom {
    Optional<Lesson> findByIdAndCoachIdAndLessonStatus(Long id, Long coachId, LessonStatus status);

    @EntityGraph(attributePaths = {"lessonSlots"}, type = EntityGraph.EntityGraphType.LOAD) // 한 번의 쿼리로 지정 경로까지 로드
    Optional<Lesson> findWithLessonSlotsById(Long id);

    @EntityGraph(attributePaths = {"lessonSlots"}, type = EntityGraph.EntityGraphType.LOAD) // 한 번의 쿼리로 지정 경로까지 로드
    List<Lesson> findAllByIdIn(List<Long> ids);
}
