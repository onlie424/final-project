package com.example.finalprojectb.DTO;

import lombok.Data;

import java.util.List;

@Data
public class AdaptiveQuizStartDTO {
    private Long attemptId;
    private Long quizId;
    private String quizTitle;
    private String currentDifficulty;
    private List<QuestionDTO> questions;
    private Boolean resumedFromPreviousAttempt = false;
}
