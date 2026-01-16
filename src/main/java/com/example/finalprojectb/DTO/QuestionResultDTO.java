package com.example.finalprojectb.DTO;

import lombok.Data;

@Data
public class QuestionResultDTO {
    private Long questionId;
    private String questionText;
    private String userAnswer;
    private String correctAnswer;
    private Boolean isCorrect;
    private Integer pointsEarned;
    private String explanation;
}