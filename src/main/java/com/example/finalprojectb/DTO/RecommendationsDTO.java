package com.example.finalprojectb.DTO;

import lombok.Data;

import java.util.List;

@Data
public class RecommendationsDTO {
    private List<String> masteryGaps;
    private List<String> suggestedTopics;
    private String achievement;
}