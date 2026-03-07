package com.example.finalprojectb.DTO;

import lombok.Data;

@Data
public class MLPredictionResponseDTO {
    private Double successProbability;
    private String recommendation;
}
