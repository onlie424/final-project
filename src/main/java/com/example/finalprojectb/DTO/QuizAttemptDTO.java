package com.example.finalprojectb.DTO;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class QuizAttemptDTO {
    private Long attemptId;
    private Long quizId;
    private String quizTitle;
    private Integer attemptNumber;
    private LocalDateTime startedAt;
    private List<QuestionDTO> questions;
}
