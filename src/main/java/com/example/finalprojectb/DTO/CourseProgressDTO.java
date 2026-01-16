package com.example.finalprojectb.DTO;

import lombok.Data;

import java.util.List;

@Data
public class CourseProgressDTO {
    private Long courseId;
    private String courseTitle;
    private Double completionPercentage;
    private Integer completedLessons;
    private Integer totalLessons;
    private List<ModuleProgressDTO> modules;
}