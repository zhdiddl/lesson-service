package com.learnhive.lessonservice.controller;

import com.learnhive.lessonservice.dto.LessonDto;
import com.learnhive.lessonservice.service.CoachLessonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/coaches")
public class CoachLessonController {

    private final CoachLessonService coachLessonService;

    @PostMapping("/lessons")
    public ResponseEntity<String> createLesson(@Valid @RequestBody LessonDto lessonForm
    ) {
        coachLessonService.createLesson(lessonForm);
        return ResponseEntity.ok("레슨 생성을 완료했습니다.");
    }

    @PutMapping("/lessons/{lessonId}")
    public ResponseEntity<String> updateLesson(@PathVariable Long lessonId,
                                               @RequestBody LessonDto updateForm) {
        coachLessonService.updateLesson(lessonId, updateForm);
        return ResponseEntity.ok("레슨 정보 업데이트가 완료되었습니다.");
    }

    @DeleteMapping("/lessons/{lessonId}")
    public ResponseEntity<String> deleteLesson(@PathVariable Long lessonId,
                                               @RequestBody LessonDto updateForm) {
        coachLessonService.updateLesson(lessonId, updateForm);
        return ResponseEntity.ok("레슨을 삭제했습니다.");
    }
}
