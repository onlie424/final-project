package com.example.finalprojectb.DTO;

import lombok.Data;

@Data
public class CreateQuizDTO {
    private Long lessonId;
    private String title;
    private String description;
    private Integer passingScore;
    private Integer timeLimitMinutes;
}
