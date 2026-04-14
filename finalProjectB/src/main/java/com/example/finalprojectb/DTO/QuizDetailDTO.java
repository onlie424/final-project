package com.example.finalprojectb.DTO;

import lombok.Data;

import java.util.List;

@Data
public class QuizDetailDTO {
    private Long id;
    private String title;
    private String description;
    private Integer passingScore;
    private List<QuestionDTO> questions;
}