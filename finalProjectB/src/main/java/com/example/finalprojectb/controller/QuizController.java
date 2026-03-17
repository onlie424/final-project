package com.example.finalprojectb.controller;

import com.example.finalprojectb.DTO.*;
import com.example.finalprojectb.repo.QuizAttemptRepository;
import com.example.finalprojectb.service.AdaptiveQuizService;
import com.example.finalprojectb.service.ModuleLockService;
import com.example.finalprojectb.service.ProficiencyService;
import com.example.finalprojectb.service.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/quizzes")
public class QuizController {

    @Autowired
    private QuizService quizService;

    @Autowired
    private AdaptiveQuizService adaptiveQuizService;

    @Autowired
    private ModuleLockService moduleLockService;

    @Autowired
    private ProficiencyService proficiencyService;

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    // ==================== ADMIN ENDPOINTS ====================

    // Create a quiz for a module
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<QuizDTO> createQuiz(@RequestBody CreateQuizDTO dto) {
        QuizDTO created = quizService.createQuiz(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // Add a question to a quiz
    @PostMapping("/{quizId}/questions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<QuestionDTO> addQuestion(
            @PathVariable Long quizId,
            @RequestBody CreateQuestionDTO dto) {
        QuestionDTO created = quizService.addQuestion(quizId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // Get all questions for a quiz (admin - includes correct answers)
    @GetMapping("/{quizId}/questions/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<QuestionDTO>> getQuestionsForAdmin(@PathVariable Long quizId) {
        List<QuestionDTO> questions = quizService.getQuestionsForAdmin(quizId);
        return ResponseEntity.ok(questions);
    }

    // Update a question's linked lesson
    @PatchMapping("/questions/{questionId}/lesson")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<QuestionDTO> updateQuestionLesson(
            @PathVariable Long questionId,
            @RequestParam(required = false) Long lessonId) {
        QuestionDTO updated = quizService.updateQuestionLesson(questionId, lessonId);
        return ResponseEntity.ok(updated);
    }

    // Delete a question
    @DeleteMapping("/questions/{questionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long questionId) {
        quizService.deleteQuestion(questionId);
        return ResponseEntity.noContent().build();
    }

    // ==================== QUIZ RETRIEVAL ====================

    // Get all quizzes for a module
    @GetMapping("/module/{moduleId}")
    public ResponseEntity<List<QuizDTO>> getQuizzesForModule(@PathVariable Long moduleId) {
        List<QuizDTO> quizzes = quizService.getQuizzesForModule(moduleId);
        return ResponseEntity.ok(quizzes);
    }

    // ==================== ADAPTIVE QUIZ ENDPOINTS ====================

    // Start an adaptive quiz (returns EASY round questions)
    @PostMapping("/{quizId}/adaptive/start")
    public ResponseEntity<AdaptiveQuizStartDTO> startAdaptiveQuiz(
            @PathVariable Long quizId,
            @RequestParam Long userId) {
        AdaptiveQuizStartDTO result = adaptiveQuizService.startAdaptiveQuiz(userId, quizId);
        return ResponseEntity.ok(result);
    }

    // Submit a round and get next round or results
    @PostMapping("/adaptive/submit-round")
    public ResponseEntity<AdaptiveRoundResultDTO> submitRound(@RequestBody SubmitAdaptiveRoundDTO dto) {
        AdaptiveRoundResultDTO result = adaptiveQuizService.submitRound(dto);
        return ResponseEntity.ok(result);
    }

    // ==================== MODULE LOCK STATUS ====================

    // Get lock status for all modules in a course
    @GetMapping("/module-locks/course/{courseId}")
    public ResponseEntity<List<ModuleLockStatusDTO>> getModuleLockStatus(
            @PathVariable Long courseId,
            @RequestParam Long userId) {
        List<ModuleLockStatusDTO> lockStatuses = moduleLockService.getCourseModuleLockStatus(userId, courseId);
        return ResponseEntity.ok(lockStatuses);
    }

    // ==================== ML PREDICTION ====================

    // Get ML prediction for a user on a specific quiz
    @GetMapping("/{quizId}/prediction")
    public ResponseEntity<MLPredictionResponseDTO> getPrediction(
            @PathVariable Long quizId,
            @RequestParam Long userId) {
        MLPredictionResponseDTO prediction = proficiencyService.getPrediction(userId, quizId);
        return ResponseEntity.ok(prediction);
    }

    // ==================== ATTEMPT HISTORY ====================

    // Get attempt history for a user on a specific quiz
    @GetMapping("/{quizId}/attempts")
    public ResponseEntity<List<QuizAttemptDTO>> getAttempts(
            @PathVariable Long quizId,
            @RequestParam Long userId) {
        List<QuizAttemptDTO> attempts = quizAttemptRepository
                .findByUserIdAndQuizIdOrderByAttemptedAtDesc(userId, quizId)
                .stream()
                .map(attempt -> {
                    QuizAttemptDTO dto = new QuizAttemptDTO();
                    dto.setAttemptId(attempt.getId());
                    dto.setQuizId(attempt.getQuiz().getId());
                    dto.setQuizTitle(attempt.getQuiz().getTitle());
                    dto.setAttemptNumber(attempt.getAttemptNumber());
                    dto.setStartedAt(attempt.getAttemptedAt());
                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(attempts);
    }

    // Exception handler
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
}
