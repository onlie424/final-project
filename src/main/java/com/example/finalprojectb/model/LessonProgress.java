package com.example.finalprojectb.model;


// ==================== LESSON PROGRESS ENTITY ====================

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * PHASE 1: LessonProgress Entity
 * Tracks which lessons a student has completed
 */
@Data
@Entity
@Table(name = "lesson_progress", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "lesson_id"})})
public class LessonProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(nullable = false, length = 20)
    private String status = "NOT_STARTED"; // NOT_STARTED, IN_PROGRESS, COMPLETED

    @Column(name = "time_spent_seconds")
    private Integer timeSpentSeconds = 0;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "last_accessed")
    private LocalDateTime lastAccessed;

    @PrePersist
    protected void onCreate() {
        lastAccessed = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastAccessed = LocalDateTime.now();
    }
}