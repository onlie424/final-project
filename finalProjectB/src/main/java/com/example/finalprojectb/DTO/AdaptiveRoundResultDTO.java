package com.example.finalprojectb.DTO;

import lombok.Data;

import java.util.List;

@Data
public class AdaptiveRoundResultDTO {
    private Integer roundScore;
    private Integer correctCount;
    private Integer totalQuestions;
    private Boolean escalated;
    private String nextDifficulty;
    private List<QuestionDTO> nextQuestions;
    private Boolean quizCompleted;
    private Boolean quizPassed;
    private List<QuestionResultDTO> questionResults;
    private List<LessonDTO> lessonsToRevisit;
    private Double mlPrediction;
    private String mlRecommendation;
}
