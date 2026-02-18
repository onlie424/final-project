package com.example.finalprojectb.service;
import com.example.finalprojectb.DTO.*;
import com.example.finalprojectb.model.*;
import com.example.finalprojectb.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ProgressService {

    @Autowired
    private LessonProgressRepository lessonProgressRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public LessonProgressDTO updateLessonProgress(UpdateLessonProgressDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Lesson lesson = lessonRepository.findById(dto.getLessonId())
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        LessonProgress progress = lessonProgressRepository
                .findByUserIdAndLessonId(dto.getUserId(), dto.getLessonId())
                .orElse(new LessonProgress());

        if (progress.getId() == null) {
            progress.setUser(user);
            progress.setLesson(lesson);
            progress.setStatus("NOT_STARTED");
            progress.setTimeSpentSeconds(0);
        }

        progress.setStatus(dto.getStatus());
        progress.setTimeSpentSeconds(progress.getTimeSpentSeconds() + dto.getTimeSpentSeconds());

        if ("COMPLETED".equals(dto.getStatus()) && progress.getCompletedAt() == null) {
            progress.setCompletedAt(LocalDateTime.now());
        }

        LessonProgress saved = lessonProgressRepository.save(progress);

        updateEnrollmentProgress(dto.getUserId(), lesson);

        return convertToLessonProgressDTO(saved);
    }

    private void updateEnrollmentProgress(Long userId, Lesson lesson) {
        Long courseId = lesson.getModule().getCourse().getId();

        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .orElse(null);

        if (enrollment != null) {
            long completedLessons = lessonProgressRepository
                    .findByUserIdAndStatus(userId, "COMPLETED")
                    .stream()
                    .filter(lp -> lp.getLesson().getModule().getCourse().getId().equals(courseId))
                    .count();

            int totalLessons = enrollment.getCourse().getTotalLessons();
            double percentage = totalLessons > 0 ? (completedLessons * 100.0) / totalLessons : 0.0;

            enrollment.setCompletionPercentage(percentage);
            enrollment.setLastAccessed(LocalDateTime.now());
            enrollmentRepository.save(enrollment);
        }
    }

    private LessonProgressDTO convertToLessonProgressDTO(LessonProgress progress) {
        LessonProgressDTO dto = new LessonProgressDTO();
        dto.setLessonId(progress.getLesson().getId());
        dto.setStatus(progress.getStatus());
        dto.setTimeSpentSeconds(progress.getTimeSpentSeconds());
        dto.setCompletedAt(progress.getCompletedAt());
        return dto;
    }
}