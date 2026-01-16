package com.example.finalprojectb.DTO;

import lombok.Data;

import java.util.List;

@Data
public class ProgressOverviewDTO {
    private String masteryScore;
    private String completionPrediction;
    private List<SubjectBreakdownDTO> subjects;
}