package com.gymcrm.workload.controller;

import com.gymcrm.workload.dto.TrainerWorkloadResponseDto;
import com.gymcrm.workload.service.TrainerWorkloadService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workload")
@PreAuthorize("isAuthenticated()")
public class TrainerWorkloadController {

    private final TrainerWorkloadService trainerWorkloadService;

    public TrainerWorkloadController(TrainerWorkloadService trainerWorkloadService) {
        this.trainerWorkloadService = trainerWorkloadService;
    }

    @GetMapping("/{trainerUsername}")
    public TrainerWorkloadResponseDto getWorkload(@PathVariable("trainerUsername") String trainerUsername) {
        return trainerWorkloadService.getTrainerWorkload(trainerUsername);
    }

    @GetMapping("/{trainerUsername}/{year}/{month}")
    public TrainerWorkloadResponseDto getWorkloadForMonth(@PathVariable("trainerUsername") String trainerUsername,
                                                          @PathVariable("year") int year,
                                                          @PathVariable("month") int month) {
        return trainerWorkloadService.getTrainerWorkload(trainerUsername, year, month);
    }
}