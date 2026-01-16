package com.example.finalprojectb.DTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CourseDTO {
    private Long id;
    private String title;
    private String description;
    private String difficulty;
    private String category;
    private Integer estimatedHours;
    private String thumbnailUrl;
    private Boolean isPublished;
    private Integer totalLessons;
    private LocalDateTime createdAt;
}