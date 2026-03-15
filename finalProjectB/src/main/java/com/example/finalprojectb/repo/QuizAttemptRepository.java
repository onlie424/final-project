package com.example.finalprojectb.repo;

import com.example.finalprojectb.model.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    // Find all attempts for quizzes in a list
    List<QuizAttempt> findByQuizIdIn(List<Long> quizIds);

    // Delete all attempts for quizzes in a list
    void deleteByQuizIdIn(List<Long> quizIds);

    // Check if user has passed a specific quiz
    @Query("SELECT CASE WHEN COUNT(qa) > 0 THEN true ELSE false END FROM QuizAttempt qa WHERE qa.user.id = :userId AND qa.quiz.id = :quizId AND qa.isPassed = true")
    boolean hasUserPassedQuiz(@Param("userId") Long userId, @Param("quizId") Long quizId);

    // Find the most recent completed, failed attempt with a passed difficulty (for resume logic)
    @Query("SELECT qa FROM QuizAttempt qa WHERE qa.user.id = :userId AND qa.quiz.id = :quizId AND qa.status = 'COMPLETED' AND qa.isPassed = false AND qa.highestPassedDifficulty IS NOT NULL ORDER BY qa.attemptedAt DESC")
    Optional<QuizAttempt> findLastResumableAttempt(@Param("userId") Long userId, @Param("quizId") Long quizId);

    // Check if user has passed ALL quizzes for a module
    @Query("SELECT CASE WHEN COUNT(DISTINCT q) = (SELECT COUNT(q2) FROM Quiz q2 WHERE q2.module.id = :moduleId) THEN true ELSE false END FROM Quiz q JOIN QuizAttempt qa ON qa.quiz = q WHERE q.module.id = :moduleId AND qa.user.id = :userId AND qa.isPassed = true")
    boolean hasUserPassedAllModuleQuizzes(@Param("userId") Long userId, @Param("moduleId") Long moduleId);
}
