package com.example.finalprojectb.service;

import com.example.finalprojectb.DTO.*;
import com.example.finalprojectb.model.*;
import com.example.finalprojectb.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public EnrollmentDTO enrollUser(CreateEnrollmentDTO dto) {
        if (enrollmentRepository.existsByUserIdAndCourseId(dto.getUserId(), dto.getCourseId())) {
            throw new RuntimeException("User already enrolled in this course");
        }

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        Enrollment enrollment = new Enrollment();
        enrollment.setUser(user);
        enrollment.setCourse(course);
        enrollment.setStatus("ACTIVE");
        enrollment.setCompletionPercentage(0.0);

        Enrollment saved = enrollmentRepository.save(enrollment);
        return convertToEnrollmentDTO(saved);
    }

    public List<EnrollmentDTO> getUserEnrollments(Long userId) {
        return enrollmentRepository.findByUserId(userId)
                .stream()
                .map(this::convertToEnrollmentDTO)
                .collect(Collectors.toList());
    }

    public EnrollmentDTO getEnrollment(Long userId, Long courseId) {
        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));
        return convertToEnrollmentDTO(enrollment);
    }

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