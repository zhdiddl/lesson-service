package com.learnhive.lessonservice.service;

import com.learnhive.lessonservice.domain.lesson.Lesson;
import com.learnhive.lessonservice.exception.CustomException;
import com.learnhive.lessonservice.exception.ExceptionCode;
import com.learnhive.lessonservice.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class LessonSearchService {

    private final LessonRepository lessonRepository;

    public List<Lesson> searchByTitle(String title, int offset, int limit) {
        return lessonRepository.searchLessonsByTitle(title, offset, limit);
    }


    public Lesson getByLessonId(Long lessonId) {
        return lessonRepository.findWithLessonSlotsById(lessonId)
                .orElseThrow(() -> new CustomException(ExceptionCode.LESSON_NOT_FOUND));
    }

    public List<Lesson> getListByLessonIds(List<Long> lessonIds) {
        return lessonRepository.findAllByIdIn(lessonIds);
    }

}
