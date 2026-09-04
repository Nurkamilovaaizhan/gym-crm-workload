package com.gymcrm.workload.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TrainerWorkloadResponseDto {
    private String trainerUsername;
    private String trainerFirstName;
    private String trainerLastName;
    private boolean isActive;
    private List<YearSummaryDto> years = new ArrayList<>();
}