package com.example.finalprojectb.DTO;

import lombok.Data;

@Data
public class ActivityDTO {
    private Long id;
    private String type;
    private String title;
    private String date;
    private Boolean completed;
}