package com.example.finalprojectb.DTO;

import lombok.Data;

import java.util.List;

@Data
public class QuestionDTO {
    private Long id;
    private String questionText;
    private String questionType;
    private Integer points;
    private String difficultyLevel;
    private List<QuestionOptionDTO> options;
}
