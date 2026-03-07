package com.example.finalprojectb.repo;

import com.example.finalprojectb.model.QuestionResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionResponseRepository extends JpaRepository<QuestionResponse, Long> {

    // Find all responses for a quiz attempt
    List<QuestionResponse> findByAttemptId(Long attemptId);

    // Find response for a specific question in an attempt
    Optional<QuestionResponse> findByAttemptIdAndQuestionId(Long attemptId, Long questionId);

    // Delete all responses for attempts in a list
    void deleteByAttemptIdIn(List<Long> attemptIds);

    // ML Feature 1: Average correctness for a user across all their responses
    @Query("SELECT AVG(CASE WHEN qr.isCorrect = true THEN 1.0 ELSE 0.0 END) FROM QuestionResponse qr WHERE qr.attempt.user.id = :userId")
    Double findUserMeanCorrect(@Param("userId") Long userId);

    // ML Feature 2: Total interaction count for a user
    @Query("SELECT COUNT(qr) FROM QuestionResponse qr WHERE qr.attempt.user.id = :userId")
    Long countUserInteractions(@Param("userId") Long userId);

    // ML Feature 3: Average correctness for a specific quiz across ALL students
    @Query("SELECT AVG(CASE WHEN qr.isCorrect = true THEN 1.0 ELSE 0.0 END) FROM QuestionResponse qr WHERE qr.attempt.quiz.id = :quizId")
    Double findSkillMeanCorrect(@Param("quizId") Long quizId);
}