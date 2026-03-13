package com.example.finalprojectb.repo;

import com.example.finalprojectb.model.QuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionOptionRepository extends JpaRepository<QuestionOption, Long> {

    // Find all options for a question, ordered by index
    List<QuestionOption> findByQuestionIdOrderByOrderIndexAsc(Long questionId);

    // Find correct option for a question
    Optional<QuestionOption> findByQuestionIdAndIsCorrectTrue(Long questionId);

    // Delete all options for a question
    void deleteByQuestionId(Long questionId);
}