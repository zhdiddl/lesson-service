package com.learnhive.lessonservice.service.coach;

import com.learnhive.lessonservice.domain.lesson.Lesson;
import com.learnhive.lessonservice.domain.lesson.LessonSlot;
import com.learnhive.lessonservice.domain.lesson.LessonStatus;
import com.learnhive.lessonservice.domain.user.UserAccount;
import com.learnhive.lessonservice.dto.LessonSlotDto;
import com.learnhive.lessonservice.exception.CustomException;
import com.learnhive.lessonservice.exception.ExceptionCode;
import com.learnhive.lessonservice.repository.LessonRepository;
import com.learnhive.lessonservice.repository.LessonSlotRepository;
import com.learnhive.lessonservice.security.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CoachLessonSlotService {

    private final AuthenticatedUserService authenticatedUserService;
    private final LessonRepository lessonRepository;
    private final LessonSlotRepository lessonSlotRepository;

    @Transactional
    public void addLessonSlot(Long lessonId, LessonSlotDto lessonSlotDto) {
        UserAccount authenticatedUser = authenticatedUserService.getAuthenticatedUser();

        // 슬롯 추가가 가능한 레슨 조회
        Lesson existingLesson = lessonRepository.findByIdAndCoachIdAndLessonStatus(
                lessonId, authenticatedUser.getId(), LessonStatus.INACTIVE)
                .orElseThrow(() -> new CustomException(ExceptionCode.LESSON_NOT_FOUND));

        // 이미 존재하는 시간대인지 확인
        if (existingLesson.getLessonSlots().stream()
                .anyMatch(slot -> slot.getStartTime().equals(lessonSlotDto.startTime()))) {
            throw new CustomException(ExceptionCode.LESSON_SLOT_ALREADY_EXISTS);
        }

        // 슬롯을 레슨에 추가
        LessonSlot newLessonSlot = LessonSlot.builder()
                .coach(authenticatedUser)
                .lesson(existingLesson)
                .startTime(lessonSlotDto.startTime())
                .quantity(lessonSlotDto.quantity())
                .build();

        existingLesson.getLessonSlots().add(newLessonSlot);
    }

    @Transactional
    public void updateLessonSlot(Long slotId, Long lessonId, LessonSlotDto lessonSlotDto) {
        UserAccount authenticatedUser = authenticatedUserService.getAuthenticatedUser();

        // 업데이트 가능한 레슨 조회
        Lesson existingLesson = lessonRepository.findByIdAndCoachIdAndLessonStatus(
                        lessonId, authenticatedUser.getId(), LessonStatus.INACTIVE)
                .orElseThrow(() -> new CustomException(ExceptionCode.LESSON_NOT_FOUND));

        // 업데이트 가능한 슬롯 조회
        LessonSlot existingLessonSlot = lessonSlotRepository
                .findByIdAndLessonIdAndCoachId(slotId, existingLesson.getId(), authenticatedUser.getId())
                .orElseThrow(() -> new CustomException(ExceptionCode.LESSON_SLOT_NOT_FOUND));

        // 시간이 중복되는 슬롯이 없는지 확인
        if (existingLesson.getLessonSlots().stream()
                .anyMatch(lessonSlot -> lessonSlot.getStartTime().equals(existingLessonSlot.getStartTime()))) {
            throw new CustomException(ExceptionCode.LESSON_SLOT_ALREADY_EXISTS);
        }

        // 변경된 값을 업데이트
        existingLessonSlot.updateStartTime(lessonSlotDto.startTime());
        existingLessonSlot.updateQuantity(lessonSlotDto.quantity());
    }

    @Transactional
    public void deleteLesson(Long lessonId, Long slotId) {
        UserAccount authenticatedUser = authenticatedUserService.getAuthenticatedUser();

        // 삭제 가능한 레슨 조회하고 활성화 상태에서는 삭제할 수 없도록 유도
        Lesson existingLesson = lessonRepository.findByIdAndCoachIdAndLessonStatus(
                        lessonId, authenticatedUser.getId(), LessonStatus.INACTIVE)
                .orElseThrow(() -> new CustomException(ExceptionCode.LESSON_NOT_FOUND));

        // 삭제 처리
        authenticatedUser.softDeleteAccount();
    }

}
