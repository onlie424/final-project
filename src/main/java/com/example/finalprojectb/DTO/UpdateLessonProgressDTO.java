package com.example.finalprojectb.DTO;

import lombok.Data;

@Data
public class UpdateLessonProgressDTO {
    private Long userId;
    private Long lessonId;
    private String status;
    private Integer timeSpentSeconds;
}
