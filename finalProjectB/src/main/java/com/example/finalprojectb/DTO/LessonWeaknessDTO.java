package com.example.finalprojectb.DTO;

import lombok.Data;

@Data
public class LessonWeaknessDTO {
    private Long lessonId;
    private String lessonTitle;
    private Long moduleId;
    private String moduleTitle;
    private Long courseId;
    private int failCount;
    private boolean quizPassed;
    /** "STRONG_REVIEW" | "REVIEW" | "PRACTICE" */
    private String severity;
}
