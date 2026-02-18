package com.example.finalprojectb.service;

import com.example.finalprojectb.DTO.*;
import com.example.finalprojectb.model.*;
import com.example.finalprojectb.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EnrollmentService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Enroll user in course
     */
    @Transactional
    public EnrollmentDTO enrollInCourse(CreateEnrollmentDTO dto) {
        // Check if already enrolled
        if (enrollmentRepository.existsByUserIdAndCourseId(dto.getUserId(), dto.getCourseId())) {
            throw new RuntimeException("User already enrolled in this course");
        }

        // Validate user exists
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Validate course exists and is published
        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if (!course.getIsPublished()) {
            throw new RuntimeException("Cannot enroll in unpublished course");
        }

        // Create enrollment
        Enrollment enrollment = new Enrollment();
        enrollment.setUser(user);
        enrollment.setCourse(course);
        enrollment.setStatus("ACTIVE");
        enrollment.setCompletionPercentage(0.0);
        enrollment.setEnrollmentDate(LocalDateTime.now());
        enrollment.setLastAccessed(LocalDateTime.now());

        Enrollment saved = enrollmentRepository.save(enrollment);
        return convertToEnrollmentDTO(saved);
    }

    /**
     * Get all enrollments for a user
     */
    public List<EnrollmentDTO> getUserEnrollments(Long userId) {
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found");
        }

        return enrollmentRepository.findByUserId(userId)
                .stream()
                .map(this::convertToEnrollmentDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all enrollments for a course (admin)
     */
    public List<EnrollmentDTO> getCourseEnrollments(Long courseId) {
        // Validate course exists
        if (!courseRepository.existsById(courseId)) {
            throw new RuntimeException("Course not found");
        }

        return enrollmentRepository.findByCourseId(courseId)
                .stream()
                .map(this::convertToEnrollmentDTO)
                .collect(Collectors.toList());
    }

    /**
     * Check if user is enrolled in course
     */
    public boolean isEnrolled(Long userId, Long courseId) {
        return enrollmentRepository.existsByUserIdAndCourseId(userId, courseId);
    }

    /**
     * Get enrollment by ID
     */
    public EnrollmentDTO getEnrollmentById(Long id) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));
        return convertToEnrollmentDTO(enrollment);
    }

    /**
     * Get enrollment by user and course
     */
    public EnrollmentDTO getEnrollment(Long userId, Long courseId) {
        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));
        return convertToEnrollmentDTO(enrollment);
    }

    /**
     * Unenroll user from course
     */
    @Transactional
    public void unenroll(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));

        enrollmentRepository.delete(enrollment);
    }

    /**
     * Update enrollment status
     */
    @Transactional
    public EnrollmentDTO updateEnrollmentStatus(Long enrollmentId, String status) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));

        // Validate status
        if (!status.equals("ACTIVE") && !status.equals("PAUSED") && !status.equals("COMPLETED")) {
            throw new RuntimeException("Invalid status. Must be ACTIVE, PAUSED, or COMPLETED");
        }

        enrollment.setStatus(status);
        enrollment.setLastAccessed(LocalDateTime.now());

        Enrollment updated = enrollmentRepository.save(enrollment);
        return convertToEnrollmentDTO(updated);
    }

    /**
     * Update enrollment progress percentage
     */
    @Transactional
    public EnrollmentDTO updateEnrollmentProgress(Long enrollmentId, Double completionPercentage) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));

        enrollment.setCompletionPercentage(completionPercentage);
        enrollment.setLastAccessed(LocalDateTime.now());

        // Auto-update status based on completion
        if (completionPercentage >= 100.0) {
            enrollment.setStatus("COMPLETED");
        }

        Enrollment updated = enrollmentRepository.save(enrollment);
        return convertToEnrollmentDTO(updated);
    }

    /**
     * Convert Enrollment entity to DTO
     */
    private EnrollmentDTO convertToEnrollmentDTO(Enrollment enrollment) {
        EnrollmentDTO dto = new EnrollmentDTO();
        dto.setId(enrollment.getId());
        dto.setUserId(enrollment.getUser().getId());
        dto.setCourseId(enrollment.getCourse().getId());
        dto.setCourseTitle(enrollment.getCourse().getTitle());
        dto.setCourseThumbnail(enrollment.getCourse().getThumbnailUrl());
        dto.setStatus(enrollment.getStatus());
        dto.setCompletionPercentage(enrollment.getCompletionPercentage());
        dto.setEnrollmentDate(enrollment.getEnrollmentDate());
        dto.setLastAccessed(enrollment.getLastAccessed());
        return dto;
    }
}