package com.example.finalprojectb.DTO;

import lombok.Data;

import java.util.List;

@Data
public class CourseDetailDTO {
    private Long id;
    private String title;
    private String description;
    private String difficulty;
    private String category;
    private Integer estimatedHours;
    private String thumbnailUrl;
    private List<ModuleDTO> modules; // Include modules
    private Integer totalLessons;
    private Boolean isEnrolled; // For logged-in user
}