package com.example.finalprojectb.DTO;

import lombok.Data;

import java.util.List;

@Data
public class DashboardDTO {
    private CurrentFocusDTO currentFocus;
    private ProgressOverviewDTO progressOverview;
    private RecommendationsDTO recommendations;
    private List<ActivityDTO> activities;
}