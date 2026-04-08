package com.example.finalprojectb.DTO;

import lombok.Data;

@Data
public class NextStepDTO {
    private Long moduleId;
    private String moduleTitle;
    private Long courseId;
    private Long firstLessonId;      // nullable — module may have no lessons yet
    private String firstLessonTitle; // nullable
}
