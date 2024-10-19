package com.learnhive.lessonservice.repository;

import com.learnhive.lessonservice.config.QuerydslConfig;
import com.learnhive.lessonservice.domain.lesson.Lesson;
import com.learnhive.lessonservice.domain.lesson.LessonSlot;
import com.learnhive.lessonservice.domain.user.UserAccount;
import com.learnhive.lessonservice.domain.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import(QuerydslConfig.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class LessonRepositoryTest {

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    private UserAccount coach;

    @BeforeEach
    void setUp() {
        coach = createUserAccount();
        userAccountRepository.save(coach);
    }

    @Test
    @DisplayName("ID로 레슨과 슬롯을 함께 조회한다.")
    void testFindWithLessonSlotsById() {
        // Given
        Lesson lesson = createLessonWithSlots("new lesson", coach);
        lessonRepository.save(lesson);

        // When
        Optional<Lesson> foundLesson = lessonRepository.findWithLessonSlotsById(lesson.getId());

        // Then
        assertTrue(foundLesson.isPresent());
        assertEquals(lesson.getId(), foundLesson.get().getId());
        assertEquals(2, foundLesson.get().getLessonSlots().size());
    }

    @Test
    @DisplayName("여러 ID로 레슨 리스트를 조회한다.")
    void testFindAllByIdIn() {
        // Given
        Lesson lesson1 = createLessonWithSlots("new lesson 1", coach);
        Lesson lesson2 = createLessonWithSlots("new lesson 2", coach);
        lessonRepository.saveAll(Arrays.asList(lesson1, lesson2));

        // When
        List<Lesson> lessons = lessonRepository.findAllByIdIn(Arrays.asList(lesson1.getId(), lesson2.getId()));

        // Then
        assertEquals(2, lessons.size());
        assertEquals(lesson1.getTitle(), lessons.get(0).getTitle());
        assertEquals(lesson2.getTitle(), lessons.get(1).getTitle());
    }

    @Test
    @DisplayName("상품명을 키워드로 검색한다.")
    void testSearchLessonsByTitle() {
        // Given
        Lesson lesson1 = createLesson("new lesson 1", coach);
        Lesson lesson2 = createLesson("new lesson 2", coach);
        lessonRepository.saveAll(Arrays.asList(lesson1, lesson2));

        // When
        List<Lesson> foundLessons = lessonRepository.searchLessonsByTitle("new", 0, 10);

        // Then
        assertEquals(2, foundLessons.size());
    }

    @Test
    @DisplayName("키워드에 맞는 상품명이 없으면 빈 리스트를 반환한다.")
    void testSearchLessonsByTitle_NoMatch() {
        // Given
        Lesson lesson1 = createLesson("new lesson 1", coach);
        lessonRepository.save(lesson1);

        // When
        List<Lesson> foundLessons = lessonRepository.searchLessonsByTitle("old", 0, 10);

        // Then
        assertEquals(0, foundLessons.size());
    }

    private Lesson createLessonWithSlots(String title, UserAccount coach) {
        Lesson lesson = Lesson.builder()
                .title(title)
                .coach(coach)
                .price(10000)
                .lessonSlots(new ArrayList<>())
                .build();

        lesson.getLessonSlots().add(createLessonSlot(lesson, 1, coach));
        lesson.getLessonSlots().add(createLessonSlot(lesson, 2, coach));

        return lesson;
    }

    private LessonSlot createLessonSlot(Lesson lesson, int dayOffset, UserAccount coach) {
        return LessonSlot.builder()
                .coach(coach)
                .lesson(lesson)
                .startTime(LocalDateTime.now().plusDays(dayOffset))
                .quantity(10)
                .build();
    }

    private Lesson createLesson(String title, UserAccount coach) {
        return Lesson.builder()
                .coach(coach)
                .title(title)
                .price(10000)
                .build();
    }

    private UserAccount createUserAccount() {
        return UserAccount.builder()
                .username("username")
                .userPassword("password")
                .email("email")
                .userRole(UserRole.COACH)
                .build();
    }
}
