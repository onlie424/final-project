package com.example.finalprojectb.DTO;

import lombok.Data;

import java.util.List;

@Data
public class LessonDetailDTO {
    private Long id;
    private String title;
    private String contentType;
    private String contentUrl;
    private String contentText;
    private Integer durationMinutes;
    private List<QuizDTO> quizzes;
    private Boolean isCompleted;
}