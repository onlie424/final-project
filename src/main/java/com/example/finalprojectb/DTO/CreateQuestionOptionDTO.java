package com.example.finalprojectb.DTO;

import lombok.Data;

@Data
public class CreateQuestionOptionDTO {
    private String optionText;
    private Boolean isCorrect;
    private Integer orderIndex;
}