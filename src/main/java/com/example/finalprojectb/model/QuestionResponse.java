package com.example.finalprojectb.model;

// ==================== QUESTION RESPONSE ENTITY ====================

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * PHASE 1: QuestionResponse Entity
 * Records individual answers (needed for ML later)
 */
@Data
@Entity
@Table(name = "question_responses")
public class QuestionResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    private QuizAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "user_answer", columnDefinition = "TEXT")
    private String userAnswer;

    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect = false;

    @Column(name = "points_earned")
    private Integer pointsEarned = 0;


    @Column(name = "time_spent_seconds")
    private Integer timeSpentSeconds;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
