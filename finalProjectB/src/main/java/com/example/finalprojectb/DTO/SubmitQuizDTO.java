package com.example.finalprojectb.DTO;

import lombok.Data;

import java.util.List;

@Data
public class SubmitQuizDTO {
    private Long attemptId;
    private List<QuizAnswerDTO> answers;
    private Integer timeTakenSeconds;
}