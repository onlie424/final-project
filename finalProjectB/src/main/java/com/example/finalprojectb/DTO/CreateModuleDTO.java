package com.example.finalprojectb.DTO;

import lombok.Data;

@Data
public class CreateModuleDTO {
    private Long courseId;
    private String title;
    private String description;
    private Integer orderIndex;
}