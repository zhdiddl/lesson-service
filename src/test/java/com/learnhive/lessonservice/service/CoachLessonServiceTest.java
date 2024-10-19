package com.learnhive.lessonservice.service;

import com.learnhive.lessonservice.domain.lesson.Lesson;
import com.learnhive.lessonservice.domain.lesson.LessonStatus;
import com.learnhive.lessonservice.domain.user.UserAccount;
import com.learnhive.lessonservice.domain.user.UserRole;
import com.learnhive.lessonservice.dto.LessonDto;
import com.learnhive.lessonservice.exception.CustomException;
import com.learnhive.lessonservice.repository.LessonRepository;
import com.learnhive.lessonservice.security.AuthenticatedUserService;
import com.learnhive.lessonservice.service.coach.CoachLessonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CoachLessonServiceTest {

    @InjectMocks
    private CoachLessonService coachLessonService;

    @Mock
    private AuthenticatedUserService authenticatedUserService;

    @Mock
    private LessonRepository lessonRepository;

    private LessonDto lessonDto;
    private UserAccount mockUser;
    private Lesson mockLesson;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockUser = UserAccount.builder()
                .id(1L)
                .username("coachUser")
                .userRole(UserRole.COACH)
                .build();

        lessonDto = new LessonDto(
                "Test Lesson",
                1000,
                "This is a test lesson",
                LessonStatus.INACTIVE
        );

        mockLesson = Lesson.builder()
                .id(1L)
                .coach(mockUser)
                .title(lessonDto.title())
                .price(lessonDto.price())
                .description(lessonDto.description())
                .lessonStatus(lessonDto.status())
                .build();
    }

    @Test
    @DisplayName("레슨 상품을 등록한다.")
    void testCreateLesson() {
        // given
        when(authenticatedUserService.getAuthenticatedUser()).thenReturn(mockUser);

        // when
        coachLessonService.createLesson(lessonDto);

        // then
        verify(lessonRepository, times(1)).save(any(Lesson.class));
    }

    @Test
    @DisplayName("레슨 상품을 수정한다.")
    void testUpdateLesson_Success() {
        // given
        Long lessonId = 1L;
        when(authenticatedUserService.getAuthenticatedUser()).thenReturn(mockUser);
        when(lessonRepository.findByIdAndCoachIdAndLessonStatus(lessonId, mockUser.getId(), LessonStatus.INACTIVE))
                .thenReturn(Optional.of(mockLesson));

        // when
        coachLessonService.updateLesson(lessonId, lessonDto);

        // then
        assertEquals(lessonDto.title(), mockLesson.getTitle());
        assertEquals(lessonDto.price(), mockLesson.getPrice());
        assertEquals(lessonDto.description(), mockLesson.getDescription());
        verify(lessonRepository, never()).save(any()); // Repository의 save 호출 없이 변경
    }

    @Test
    @DisplayName("레슨 상품을 수정에 실패한다.")
    void testUpdateLesson_NotFound() {
        // given
        Long lessonId = 1L;
        when(authenticatedUserService.getAuthenticatedUser()).thenReturn(mockUser);
        when(lessonRepository.findByIdAndCoachIdAndLessonStatus(lessonId, mockUser.getId(), LessonStatus.INACTIVE))
                .thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            coachLessonService.updateLesson(lessonId, lessonDto);
        });
        assertEquals("LESSON_NOT_FOUND", exception.getExceptionCode().name());
    }

    @Test
    @DisplayName("레슨 상품을 삭제한다.")
    void testDeleteLesson_Success() {
        // given
        Long lessonId = 1L;
        when(authenticatedUserService.getAuthenticatedUser()).thenReturn(mockUser);
        when(lessonRepository.findByIdAndCoachIdAndLessonStatus(lessonId, mockUser.getId(), LessonStatus.INACTIVE))
                .thenReturn(Optional.of(mockLesson));

        // when
        coachLessonService.deleteLesson(lessonId);

        // then
        verify(lessonRepository, times(1)).delete(mockLesson);
    }

    @Test
    @DisplayName("레슨 상품을 삭제에 실패한다.")
    void testDeleteLesson_NotFound() {
        // given
        Long lessonId = 1L;
        when(authenticatedUserService.getAuthenticatedUser()).thenReturn(mockUser);
        when(lessonRepository.findByIdAndCoachIdAndLessonStatus(lessonId, mockUser.getId(), LessonStatus.INACTIVE))
                .thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            coachLessonService.deleteLesson(lessonId);
        });
        assertEquals("LESSON_NOT_FOUND", exception.getExceptionCode().name());
    }
}
