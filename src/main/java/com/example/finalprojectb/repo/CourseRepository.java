package com.example.finalprojectb.repo;

import com.example.finalprojectb.model.Course;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    // Find published courses only
    List<Course> findByIsPublishedTrue();

    // Find courses by category
    List<Course> findByCategory(String category);

    // Find courses by difficulty
    List<Course> findByDifficulty(String difficulty);

    // Search courses by title (case-insensitive)
    List<Course> findByTitleContainingIgnoreCase(String keyword);
}