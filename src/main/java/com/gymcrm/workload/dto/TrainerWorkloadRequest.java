package com.gymcrm.workload.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gymcrm.workload.constants.enums.ActionType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TrainerWorkloadRequest {

    @NotBlank
    private String trainerUsername;

    @NotBlank
    private String trainerFirstName;

    @NotBlank
    private String trainerLastName;

    @JsonProperty("isActive")
    private boolean active;

    @NotNull
    private LocalDateTime trainingDate;

    @Min(1)
    private int trainingDuration;

    @NotNull
    private ActionType actionType;
}