package com.example.finalprojectb.repo;

import com.example.finalprojectb.model.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    // Find all attempts for a user and quiz
    List<QuizAttempt> findByUserIdAndQuizIdOrderByAttemptedAtDesc(Long userId, Long quizId);

    // Find all attempts for a user
    List<QuizAttempt> findByUserIdOrderByAttemptedAtDesc(Long userId);

    // Find user's best score for a quiz
    Optional<QuizAttempt> findFirstByUserIdAndQuizIdOrderByScoreDesc(Long userId, Long quizId);

    // Count attempts for a quiz by a user
    Long countByUserIdAndQuizId(Long userId, Long quizId);
}
