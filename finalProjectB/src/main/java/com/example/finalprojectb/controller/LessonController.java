package com.example.finalprojectb.controller;

import com.example.finalprojectb.DTO.CreateLessonDTO;
import com.example.finalprojectb.DTO.LessonDTO;
import com.example.finalprojectb.service.LessonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lessons")
public class LessonController {

    @Autowired
    private LessonService lessonService;

    // Create new lesson
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LessonDTO> createLesson(@RequestBody CreateLessonDTO dto) {
        LessonDTO created = lessonService.createLesson(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // Get lesson detail
    @GetMapping("/{id}")
    public ResponseEntity<LessonDTO> getLessonById(
            @PathVariable Long id,
            @RequestParam(required = false) Long userId) {
        LessonDTO lesson = lessonService.getLessonById(id, userId);
        return ResponseEntity.ok(lesson);
    }

    // Update lesson
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LessonDTO> updateLesson(
            @PathVariable Long id,
            @RequestBody CreateLessonDTO dto) {
        LessonDTO updated = lessonService.updateLesson(id, dto);
        return ResponseEntity.ok(updated);
    }

    // Get all lessons for a module
    @GetMapping("/module/{moduleId}")
    public ResponseEntity<List<LessonDTO>> getLessonsByModule(@PathVariable Long moduleId) {
        List<LessonDTO> lessons = lessonService.getLessonsByModule(moduleId);
        return ResponseEntity.ok(lessons);
    }

    // Delete lesson
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteLesson(@PathVariable Long id) {
        lessonService.deleteLesson(id);
        return ResponseEntity.noContent().build();
    }

    // Exception handler
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
}