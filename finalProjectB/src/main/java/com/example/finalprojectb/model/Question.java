package com.example.finalprojectb.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Data
@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(name = "question_text", columnDefinition = "TEXT", nullable = false)
    private String questionText;

    @Column(name = "question_type", nullable = false, length = 50)
    private String questionType; // "MULTIPLE_CHOICE", "TRUE_FALSE", "SHORT_ANSWER"

    @Column(name = "correct_answer", columnDefinition = "TEXT")
    private String correctAnswer; // For short answer/true-false

    @Column(columnDefinition = "TEXT")
    private String explanation; // Explanation of correct answer

    @Column(name = "order_index")
    private Integer orderIndex;

    @Column(name = "points")
    private Integer points = 1; // Points for this question

    @Column(name = "difficulty_level", length = 20)
    private String difficultyLevel = "EASY"; // "EASY", "MEDIUM", "HARD"

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Relationship with options (for multiple choice)
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<QuestionOption> options = new ArrayList<>();

    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}