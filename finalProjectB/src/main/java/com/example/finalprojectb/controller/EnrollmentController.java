package com.example.finalprojectb.controller;

import com.example.finalprojectb.DTO.EnrollmentDTO;
import com.example.finalprojectb.DTO.CreateEnrollmentDTO;
import com.example.finalprojectb.service.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    @Autowired
    private EnrollmentService enrollmentService;

    /**
     * Enroll in a course
     * POST /api/enrollments/enroll
     */
    @PostMapping("/enroll")
    public ResponseEntity<EnrollmentDTO> enrollInCourse(@RequestBody CreateEnrollmentDTO createEnrollmentDTO) {
        EnrollmentDTO enrollment = enrollmentService.enrollInCourse(createEnrollmentDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(enrollment);
    }

    /**
     * Get all enrollments for a user
     * GET /api/enrollments/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<EnrollmentDTO>> getUserEnrollments(@PathVariable Long userId) {
        List<EnrollmentDTO> enrollments = enrollmentService.getUserEnrollments(userId);
        return ResponseEntity.ok(enrollments);
    }

    /**
     * Get all enrollments for a course (admin only)
     * GET /api/enrollments/course/{courseId}
     */
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EnrollmentDTO>> getCourseEnrollments(@PathVariable Long courseId) {
        List<EnrollmentDTO> enrollments = enrollmentService.getCourseEnrollments(courseId);
        return ResponseEntity.ok(enrollments);
    }

    /**
     * Check if user is enrolled in a course
     * GET /api/enrollments/check?userId=X&courseId=Y
     */
    @GetMapping("/check")
    public ResponseEntity<Boolean> checkEnrollment(
            @RequestParam Long userId,
            @RequestParam Long courseId) {
        boolean isEnrolled = enrollmentService.isEnrolled(userId, courseId);
        return ResponseEntity.ok(isEnrolled);
    }

    /**
     * Get specific enrollment
     * GET /api/enrollments/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<EnrollmentDTO> getEnrollmentById(@PathVariable Long id) {
        EnrollmentDTO enrollment = enrollmentService.getEnrollmentById(id);
        return ResponseEntity.ok(enrollment);
    }

    /**
     * Unenroll from a course
     * DELETE /api/enrollments/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> unenroll(@PathVariable Long id) {
        enrollmentService.unenroll(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Update enrollment status (admin only)
     * PUT /api/enrollments/{id}/status
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EnrollmentDTO> updateEnrollmentStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        EnrollmentDTO enrollment = enrollmentService.updateEnrollmentStatus(id, status);
        return ResponseEntity.ok(enrollment);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}