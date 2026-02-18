package com.example.finalprojectb.DTO;

import lombok.Data;

@Data
public class CreateCourseDTO {
    private String title;
    private String description;
    private String difficulty;
    private String category;
    private Integer estimatedHours;
    private String thumbnailUrl;
}
