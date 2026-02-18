package com.example.finalprojectb.DTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EnrollmentDTO {
    private Long id;
    private Long userId;
    private Long courseId;
    private String courseTitle;
    private String courseThumbnail;
    private String status;
    private Double completionPercentage;
    private LocalDateTime enrollmentDate;
    private LocalDateTime lastAccessed;
}
