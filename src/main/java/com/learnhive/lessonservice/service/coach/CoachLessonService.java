package com.learnhive.lessonservice.service.coach;

import com.learnhive.lessonservice.domain.lesson.Lesson;
import com.learnhive.lessonservice.domain.lesson.LessonStatus;
import com.learnhive.lessonservice.domain.user.UserAccount;
import com.learnhive.lessonservice.dto.LessonDto;
import com.learnhive.lessonservice.exception.CustomException;
import com.learnhive.lessonservice.exception.ExceptionCode;
import com.learnhive.lessonservice.repository.LessonRepository;
import com.learnhive.lessonservice.security.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CoachLessonService {

    private final AuthenticatedUserService authenticatedUserService;
    private final LessonRepository lessonRepository;

    @Transactional
    public void createLesson(LessonDto lessonDto) {
        UserAccount authenticatedUser = authenticatedUserService.getAuthenticatedUser();

        // 객체 생성
        Lesson newLesson = Lesson.builder()
                .coach(authenticatedUser)
                .title(lessonDto.title())
                .price(lessonDto.price())
                .description(lessonDto.description())
                .lessonStatus(lessonDto.status())
                .build();

        lessonRepository.save(newLesson);
    }

    @Transactional
    public void updateLesson(Long lessonId, LessonDto lessonDto) {
        UserAccount authenticatedUser = authenticatedUserService.getAuthenticatedUser();

        // 업데이트 가능한 레슨 조회
        Lesson existingLesson = lessonRepository.findByIdAndCoachIdAndLessonStatus(
                        lessonId, authenticatedUser.getId(), LessonStatus.INACTIVE)
                .orElseThrow(() -> new CustomException(ExceptionCode.LESSON_NOT_FOUND));

        // 변경된 값을 업데이트
        existingLesson.updateTitle(lessonDto.title());
        existingLesson.updatePrice(lessonDto.price());
        existingLesson.updateDescription(lessonDto.description());
        existingLesson.updateStatus(lessonDto.status());
    }

    @Transactional
    public void deleteLesson(Long lessonId) {
        UserAccount authenticatedUser = authenticatedUserService.getAuthenticatedUser();

        // 삭제 가능한 레슨 확인
        Lesson existingLesson = lessonRepository.findByIdAndCoachIdAndLessonStatus(
                        lessonId, authenticatedUser.getId(), LessonStatus.INACTIVE)
                .orElseThrow(() -> new CustomException(ExceptionCode.LESSON_NOT_FOUND));

        // 삭제 처리
        lessonRepository.delete(existingLesson);
    }

}
