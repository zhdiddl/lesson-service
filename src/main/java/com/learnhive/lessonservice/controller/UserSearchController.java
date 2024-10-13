package com.learnhive.lessonservice.controller;

import com.learnhive.lessonservice.domain.lesson.Lesson;
import com.learnhive.lessonservice.service.LessonSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserSearchController {

    private final LessonSearchService lessonSearchService;

    @GetMapping("/lessons/search")
    public ResponseEntity<List<Lesson>> searchLessonsByTitle(
            @RequestParam String title,
            @RequestParam(defaultValue = "0") int offset, // 기본값 0 (첫 페이지)
            @RequestParam(defaultValue = "5") int limit   // 기본값 5 (한 번에 5개씩)
    ) {
        List<Lesson> lessons = lessonSearchService.searchByTitle(title, offset, limit);
        return ResponseEntity.ok(lessons);
    }
}
