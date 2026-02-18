package com.example.finalprojectb.repo;

import com.example.finalprojectb.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {

    // Find all quizzes for a lesson
    List<Quiz> findByLessonId(Long lessonId);

    // Find quiz by lesson (assuming one quiz per lesson for now)
    Optional<Quiz> findFirstByLessonId(Long lessonId);

    // Find all quizzes for lessons in a list
    List<Quiz> findByLessonIdIn(List<Long> lessonIds);
}