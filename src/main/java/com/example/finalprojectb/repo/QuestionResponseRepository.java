package com.example.finalprojectb.repo;

import com.example.finalprojectb.model.QuestionResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionResponseRepository extends JpaRepository<QuestionResponse, Long> {

    // Find all responses for a quiz attempt
    List<QuestionResponse> findByAttemptId(Long attemptId);

    // Find response for a specific question in an attempt
    Optional<QuestionResponse> findByAttemptIdAndQuestionId(Long attemptId, Long questionId);

    // Find all responses by a user (for ML later)
    // HINT: Join through attempt to get user
    // We'll handle this with custom queries later
}