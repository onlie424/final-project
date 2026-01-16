package com.example.finalprojectb.DTO;

import lombok.Data;

@Data
public class CurrentFocusDTO {
    private Long courseId;
    private String courseName;
    private Integer progress;
    private String estimatedCompletion;
    private String difficulty;
}
