package com.example.finalprojectb.DTO;

import lombok.Data;

import java.util.List;

@Data
public class QuizResultDTO {
    private Long attemptId;
    private Integer score;
    private Integer totalQuestions;
    private Integer correctAnswers;
    private Boolean isPassed;
    private Integer timeTakenSeconds;
    private List<QuestionResultDTO> questionResults;
}
