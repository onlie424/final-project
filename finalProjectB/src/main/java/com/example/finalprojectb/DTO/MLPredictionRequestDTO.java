package com.example.finalprojectb.DTO;

import lombok.Data;

@Data
public class MLPredictionRequestDTO {
    private Double userMeanCorrect;
    private Long userInteractionCount;
    private Double skillMeanCorrect;
}
