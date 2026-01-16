package com.example.finalprojectb.DTO;

import lombok.Data;

import java.util.List;

@Data
public class QuestionDetailDTO {
    private Long id;
    private String questionText;
    private String questionType;
    private String explanation;
    private Integer points;
    private List<QuestionOptionDTO> options;
    private String correctAnswer; // Only for admin/after submission
}
