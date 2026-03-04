package com.example.finalprojectb.repo;

import com.example.finalprojectb.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {

    // Find all quizzes for a module (ordered by quiz order)
    List<Quiz> findByModuleIdOrderByOrderIndexAsc(Long moduleId);

    // Find first quiz for a module
    Optional<Quiz> findFirstByModuleId(Long moduleId);

    // Find all quizzes for modules in a list
    List<Quiz> findByModuleIdIn(List<Long> moduleIds);
}
