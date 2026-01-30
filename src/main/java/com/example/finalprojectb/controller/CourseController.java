package com.example.finalprojectb.controller;
import com.example.finalprojectb.DTO.CourseDTO;
import com.example.finalprojectb.DTO.CourseDetailDTO;
import com.example.finalprojectb.DTO.CreateCourseDTO;
import com.example.finalprojectb.DTO.ModuleDTO;
import com.example.finalprojectb.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;

    // Get all published courses
    @GetMapping
    public ResponseEntity<List<CourseDTO>> getAllCourses() {
        List<CourseDTO> courses = courseService.getAllPublishedCourses();
        return ResponseEntity.ok(courses);
    }

    // Get course by ID with details
    @GetMapping("/{id}")
    public ResponseEntity<CourseDetailDTO> getCourseById(
            @PathVariable Long id,
            @RequestParam(required = false) Long userId) {
        CourseDetailDTO course = courseService.getCourseDetail(id, userId);
        return ResponseEntity.ok(course);
    }

    // Create new course (Admin only)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseDTO> createCourse(@RequestBody CreateCourseDTO dto) {
        CourseDTO created = courseService.createCourse(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // Update course
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseDTO> updateCourse(
            @PathVariable Long id,
            @RequestBody CreateCourseDTO dto) {
        CourseDTO updated = courseService.updateCourse(id, dto);
        return ResponseEntity.ok(updated);
    }

    // Publish course
    @PutMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> publishCourse(@PathVariable Long id) {
        courseService.publishCourse(id);
        return ResponseEntity.ok().build();
    }

    // Unpublish course
    @PutMapping("/{id}/unpublish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> unpublishCourse(@PathVariable Long id) {
        courseService.unpublishCourse(id);
        return ResponseEntity.ok().build();
    }

    // Get modules for a course
    @GetMapping("/{id}/modules")
    public ResponseEntity<List<ModuleDTO>> getCourseModules(@PathVariable Long id) {
        List<ModuleDTO> modules = courseService.getCourseModules(id);
        return ResponseEntity.ok(modules);
    }

    // Delete course (Admin only)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }

    // Search courses
    @GetMapping("/search")
    public ResponseEntity<List<CourseDTO>> searchCourses(@RequestParam String keyword) {
        List<CourseDTO> courses = courseService.searchCourses(keyword);
        return ResponseEntity.ok(courses);
    }

    // Get courses by category
    @GetMapping("/category/{category}")
    public ResponseEntity<List<CourseDTO>> getCoursesByCategory(@PathVariable String category) {
        List<CourseDTO> courses = courseService.getCoursesByCategory(category);
        return ResponseEntity.ok(courses);
    }

    // Get courses by difficulty
    @GetMapping("/difficulty/{difficulty}")
    public ResponseEntity<List<CourseDTO>> getCoursesByDifficulty(@PathVariable String difficulty) {
        List<CourseDTO> courses = courseService.getCoursesByDifficulty(difficulty);
        return ResponseEntity.ok(courses);
    }

    // Get all courses including unpublished (Admin only)
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CourseDTO>> getAllCoursesAdmin() {
        List<CourseDTO> courses = courseService.getAllCourses();
        return ResponseEntity.ok(courses);
    }

    // Exception handler
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
}