package com.learnhive.lessonservice.service;

import com.learnhive.lessonservice.domain.Lesson;
import com.learnhive.lessonservice.domain.UserAccount;
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
        Lesson newLesson = Lesson.of(authenticatedUser, lessonDto.getTitle(), lessonDto.getPrice(), lessonDto.getDescription());
        lessonRepository.save(newLesson);
    }

    @Transactional
    public void updateLesson(Long lessonId, LessonDto lessonDto) {
        UserAccount authenticatedUser = authenticatedUserService.getAuthenticatedUser();

        // 레슨 조회
        Lesson existingLesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new CustomException(ExceptionCode.LESSON_NOT_FOUND));

        // 변경된 값을 업데이트
        if (lessonDto.getTitle() != null && !lessonDto.getTitle().equals(existingLesson.getTitle())) {
            existingLesson.setTitle(lessonDto.getTitle());
        }
        if (lessonDto.getPrice() != null && !lessonDto.getPrice().equals(existingLesson.getPrice())) {
            existingLesson.setPrice(lessonDto.getPrice());
        }
        if (lessonDto.getDescription() != null && !lessonDto.getDescription().equals(existingLesson.getDescription())) {
            existingLesson.setDescription(lessonDto.getDescription());
        }
    }

    @Transactional
    public void deleteLesson(Long lessonId) {
        UserAccount authenticatedUser = authenticatedUserService.getAuthenticatedUser();

        // 레슨 조회
        Lesson existingLesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new CustomException(ExceptionCode.LESSON_NOT_FOUND));

        // 본인이 만든 레슨인지 확인
        if (!existingLesson.getCoach().equals(authenticatedUser)) {
            throw new CustomException(ExceptionCode.FORBIDDEN_ACTION);
        }
//
//        // 예약 고객이 이미 있는지 확인
//        boolean hasOrders = reservationRepository.existsByLesson(existingLesson);
//        if (hasOrders) {
//            throw new CustomException(ExceptionCode.CANNOT_DELETE_PURCHASED_LESSON);
//        }

        // 삭제 처리
        lessonRepository.delete(existingLesson);
    }

}
