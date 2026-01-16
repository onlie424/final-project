package com.example.finalprojectb.DTO;

import lombok.Data;

@Data
public class ModuleProgressDTO {
    private Long moduleId;
    private String moduleTitle;
    private Integer completedLessons;
    private Integer totalLessons;
}
