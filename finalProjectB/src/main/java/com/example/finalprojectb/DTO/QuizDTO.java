package com.example.finalprojectb.DTO;

import lombok.Data;

@Data
public class QuizDTO {
    private Long id;
    private String title;
    private String description;
    private Integer passingScore;
    private Integer questionCount;
}