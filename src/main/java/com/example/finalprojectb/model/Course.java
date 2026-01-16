package com.example.finalprojectb.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * PHASE 1: Course Entity
 * Basic course information for content management
 */
@Data
@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 50)
    private String difficulty; // "Beginner", "Intermediate", "Advanced"

    @Column(length = 100)
    private String category; // "Math", "Science", "Programming"

    @Column(name = "estimated_hours")
    private Integer estimatedHours;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "is_published")
    private Boolean isPublished = false;

    @Column(name = "total_lessons")
    private Integer totalLessons = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationship with modules
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<Module> modules = new ArrayList<>();

    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper method
    public void calculateTotalLessons() {
        this.totalLessons = modules.stream()
                .mapToInt(module -> module.getLessons().size())
                .sum();
    }
}