package com.example.finalprojectb.repo;

import com.example.finalprojectb.model.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {

    // Find progress for a user and lesson
    Optional<LessonProgress> findByUserIdAndLessonId(Long userId, Long lessonId);

    // Find all lesson progress for a user
    List<LessonProgress> findByUserId(Long userId);

    // Find completed lessons for a user
    List<LessonProgress> findByUserIdAndStatus(Long userId, String status);

    // Count completed lessons for a user in a specific course
    // HINT: You'll need to join through lesson -> module -> course
    // For now, we'll handle this in the service layer
}