package com.example.finalprojectb.DTO;

import lombok.Data;

@Data
public class CreateLessonDTO {
    private Long moduleId;
    private String title;
    private String contentType;
    private String contentUrl;
    private String contentText;
    private Integer durationMinutes;
    private Integer orderIndex;
}