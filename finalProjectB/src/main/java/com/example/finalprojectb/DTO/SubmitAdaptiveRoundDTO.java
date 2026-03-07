package com.example.finalprojectb.DTO;

import lombok.Data;

import java.util.List;

@Data
public class SubmitAdaptiveRoundDTO {
    private Long attemptId;
    private String difficulty;
    private List<QuizAnswerDTO> answers;
    private Integer timeTakenSeconds;
}
