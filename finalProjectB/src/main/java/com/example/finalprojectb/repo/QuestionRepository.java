package com.example.finalprojectb.repo;
import com.example.finalprojectb.model.Question;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    // Find all questions for a quiz, ordered by index
    List<Question> findByQuizIdOrderByOrderIndexAsc(Long quizId);

    // Find questions by type
    List<Question> findByQuestionType(String questionType);

    // Count questions in a quiz
    Long countByQuizId(Long quizId);

    // Find questions by quiz and difficulty level (for adaptive quiz rounds)
    List<Question> findByQuizIdAndDifficultyLevel(Long quizId, String difficultyLevel);
}
