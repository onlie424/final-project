package com.example.finalprojectb.DTO;

import lombok.Data;

@Data
public class QuestionOptionDTO {
    private Long id;
    private String optionText;
    private Integer orderIndex;
    // Note: isCorrect is NOT included for students
}
