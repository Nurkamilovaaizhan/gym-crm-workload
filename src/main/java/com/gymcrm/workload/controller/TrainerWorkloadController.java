package com.gymcrm.workload.controller;

import com.gymcrm.workload.dto.TrainerWorkloadRequest;
import com.gymcrm.workload.dto.TrainerWorkloadResponseDto;
import com.gymcrm.workload.service.TrainerWorkloadService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
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

    @PostMapping
    public ResponseEntity<Void> acceptWorkload(@Valid @RequestBody TrainerWorkloadRequest request) {
        trainerWorkloadService.acceptWorkload(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{trainerUsername}")
    public TrainerWorkloadResponseDto getWorkload(@PathVariable String trainerUsername) {
        return trainerWorkloadService.getTrainerWorkload(trainerUsername);
    }

    @GetMapping("/{trainerUsername}/{year}/{month}")
    public TrainerWorkloadResponseDto getWorkloadForMonth(@PathVariable String trainerUsername,
                                                          @PathVariable int year,
                                                          @PathVariable int month) {
        return trainerWorkloadService.getTrainerWorkload(trainerUsername, year, month);
    }
}