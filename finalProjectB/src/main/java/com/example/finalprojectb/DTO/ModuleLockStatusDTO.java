package com.example.finalprojectb.DTO;

import lombok.Data;

@Data
public class ModuleLockStatusDTO {
    private Long moduleId;
    private String moduleTitle;
    private Integer orderIndex;
    private Boolean isLocked;
    private String lockReason;
    private Double mlReadinessProbability;
    private Boolean prerequisiteQuizPassed;
}
