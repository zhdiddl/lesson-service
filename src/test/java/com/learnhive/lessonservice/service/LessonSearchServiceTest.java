package com.learnhive.lessonservice.service;

import com.learnhive.lessonservice.domain.lesson.Lesson;
import com.learnhive.lessonservice.repository.LessonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class LessonSearchServiceTest {

    @Mock
    private LessonRepository lessonRepository;

    @InjectMocks
    private LessonSearchService lessonSearchService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("상품명을 키워드로 검색한다.")
    void testSearchByTitle_Success() {
        // Given
        Lesson lesson1 = Lesson.builder().id(1L).title("백엔드 프로그래밍 기초 클래스").build();
        Lesson lesson2 = Lesson.builder().id(2L).title("백엔드 프로그래밍 중급 클래스").build();
        List<Lesson> mockLessons = Arrays.asList(lesson1, lesson2);

        when(lessonRepository.searchLessonsByTitle(anyString(), anyInt(), anyInt()))
                .thenReturn(mockLessons);

        // When
        String title = "백엔드";
        int offset = 0;
        int limit = 10;
        List<Lesson> result = lessonSearchService.searchByTitle(title, offset, limit);

        // Then
        assertEquals(2, result.size());
        assertEquals("백엔드 프로그래밍 기초 클래스", result.get(0).getTitle());
        assertEquals("백엔드 프로그래밍 중급 클래스", result.get(1).getTitle());

        verify(lessonRepository, times(1)).searchLessonsByTitle(title, offset, limit);
    }

    @Test
    @DisplayName("키워드에 맞는 상품명이 없어서 검색에 실패한다.")
    void testSearchByTitle_Failure() {
        // Given
        Lesson lesson1 = Lesson.builder().id(1L).title("백엔드 프로그래밍 기초 클래스").build();
        Lesson lesson2 = Lesson.builder().id(2L).title("백엔드 프로그래밍 중급 클래스").build();
        List<Lesson> mockLessons = Arrays.asList(lesson1, lesson2);

        when(lessonRepository.searchLessonsByTitle("고급", 0 ,10))
                .thenReturn(Collections.emptyList());

        // When
        String title = "고급";
        int offset = 0;
        int limit = 10;
        List<Lesson> result = lessonSearchService.searchByTitle(title, offset, limit);

        // Then
        assertEquals(0, result.size());

        verify(lessonRepository, times(1)).searchLessonsByTitle(title, offset, limit);
    }
}
