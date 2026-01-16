package com.example.finalprojectb.DTO;

import lombok.Data;

import java.util.List;

@Data
public class CreateQuestionDTO {
    private Long quizId;
    private String questionText;
    private String questionType;
    private String correctAnswer; // For TRUE_FALSE, SHORT_ANSWER
    private String explanation;
    private Integer points;
    private Integer orderIndex;
    private List<CreateQuestionOptionDTO> options; // For MULTIPLE_CHOICE
}