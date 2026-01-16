package com.example.finalprojectb.DTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LessonProgressDTO {
    private Long lessonId;
    private String status;
    private Integer timeSpentSeconds;
    private LocalDateTime completedAt;
}
