package com.example.finalprojectb.DTO;

import lombok.Data;

@Data
public class QuizAnswerDTO {
    private Long questionId;
    private String userAnswer; // For SHORT_ANSWER, TRUE_FALSE
    private Long selectedOptionId; // For MULTIPLE_CHOICE
    private Integer timeSpentSeconds;
}
