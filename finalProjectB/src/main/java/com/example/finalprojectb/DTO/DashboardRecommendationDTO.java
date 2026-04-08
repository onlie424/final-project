package com.example.finalprojectb.DTO;

import lombok.Data;

import java.util.List;

@Data
public class DashboardRecommendationDTO {
    /** Lessons the user is struggling with, sorted by failCount desc (max 5). */
    private List<LessonWeaknessDTO> weakLessons;
    /** Next module/lesson to progress into. Null if the course is fully complete. */
    private NextStepDTO nextStep;
}
