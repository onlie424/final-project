package com.example.finalprojectb.model;


// ==================== QUIZ ATTEMPT ENTITY ====================

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * PHASE 1: QuizAttempt Entity
 * Records each time a student takes a quiz
 */
@Data
@Entity
@Table(name = "quiz_attempts")
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(name = "attempt_number")
    private Integer attemptNumber = 1;

    @Column(nullable = false)
    private Integer score = 0; // Percentage 0-100

    @Column(name = "total_questions")
    private Integer totalQuestions;

    @Column(name = "correct_answers")
    private Integer correctAnswers = 0;

    @Column(name = "time_taken_seconds")
    private Integer timeTakenSeconds;

    @Column(name = "attempted_at", nullable = false)
    private LocalDateTime attemptedAt;

    @Column(name = "is_passed")
    private Boolean isPassed = false;

    @PrePersist
    protected void onCreate() {
        attemptedAt = LocalDateTime.now();
    }

    // Helper method to calculate score
    public void calculateScore() {
        if (totalQuestions > 0) {
            this.score = (int) ((correctAnswers * 100.0) / totalQuestions);
            this.isPassed = this.score >= 70; // Assuming 70% is passing
        }
    }
}
