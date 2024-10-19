package com.learnhive.lessonservice.controller;

import com.learnhive.lessonservice.dto.LessonDto;
import com.learnhive.lessonservice.dto.LessonSlotDto;
import com.learnhive.lessonservice.service.coach.CoachLessonService;
import com.learnhive.lessonservice.service.coach.CoachLessonSlotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/coaches")
public class CoachLessonController {

    private final CoachLessonService coachLessonService;
    private final CoachLessonSlotService coachLessonSlotService;

    @PostMapping("/lessons")
    public ResponseEntity<String> createLesson(@Valid @RequestBody LessonDto lessonForm
    ) {
        coachLessonService.createLesson(lessonForm);
        return ResponseEntity.ok("레슨 생성을 완료했습니다.");
    }

    @PostMapping("/lessons/{lessonId}/slots")
    public ResponseEntity<String> addLessonSlot(@PathVariable Long lessonId,
                                                @Valid @RequestBody LessonSlotDto lessonSlotForm
    ) {
        coachLessonSlotService.addLessonSlot(lessonId, lessonSlotForm);
        return ResponseEntity.ok("레슨 슬롯 추가를 완료했습니다.");
    }

    @PutMapping("/lessons/{lessonId}")
    public ResponseEntity<String> updateLesson(@PathVariable Long lessonId,
                                               @Valid @RequestBody LessonDto updateForm) {
        coachLessonService.updateLesson(lessonId, updateForm);
        return ResponseEntity.ok("레슨 정보 업데이트가 완료되었습니다.");
    }

    @PutMapping("/lessons/{lessonId}/slots/{slotId}")
    public ResponseEntity<String> updateLessonSlot(@PathVariable Long lessonId,
                                                   @PathVariable Long slotId,
                                                   @Valid @RequestBody LessonSlotDto updateForm) {
        coachLessonSlotService.updateLessonSlot(lessonId, slotId, updateForm);
        return ResponseEntity.ok("레슨 슬롯 정보 업데이트가 완료되었습니다.");
    }

    @DeleteMapping("/lessons/{lessonId}")
    public ResponseEntity<String> deleteLesson(@PathVariable Long lessonId) {
        coachLessonService.deleteLesson(lessonId);
        return ResponseEntity.ok("레슨을 삭제했습니다.");
    }

    @DeleteMapping("/lessons/{lessonId}/slots/{slotId}")
    public ResponseEntity<String> deleteLessonSlot(@PathVariable Long lessonId,
                                                   @PathVariable Long slotId) {
        coachLessonSlotService.deleteLesson(lessonId, slotId);
        return ResponseEntity.ok("레슨 슬롯을 삭제했습니다.");
    }
}
