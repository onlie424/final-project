package com.example.finalprojectb.DTO;

import lombok.Data;

@Data
public class LessonDTO {
    private Long id;
    private String title;
    private String contentType;
    private String contentUrl;
    private Integer durationMinutes;
    private Integer orderIndex;
    private Boolean isCompleted; // For logged-in user
    private String contentText;
}
