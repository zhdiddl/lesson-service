package com.learnhive.lessonservice.repository;

import com.learnhive.lessonservice.domain.lesson.Lesson;

import java.util.List;

public interface LessonRepositoryCustom {
    List<Lesson> searchLessonsByTitle(String title, int offset, int limit);
}

