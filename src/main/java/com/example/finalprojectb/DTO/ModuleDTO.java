package com.example.finalprojectb.DTO;

import lombok.Data;

import java.util.List;

@Data
public class ModuleDTO {
    private Long id;
    private String title;
    private String description;
    private Integer orderIndex;
    private List<LessonDTO> lessons; // Include lessons
}