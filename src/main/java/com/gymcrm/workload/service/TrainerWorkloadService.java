package com.gymcrm.workload.service;

import com.gymcrm.workload.constants.enums.ActionType;
import com.gymcrm.workload.dto.MonthSummaryDto;
import com.gymcrm.workload.dto.TrainerWorkloadRequest;
import com.gymcrm.workload.dto.TrainerWorkloadResponseDto;
import com.gymcrm.workload.dto.YearSummaryDto;
import com.gymcrm.workload.entity.TrainerWorkload;
import com.gymcrm.workload.repository.TrainerWorkloadRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TrainerWorkloadService {

    private final TrainerWorkloadRepository trainerWorkloadRepository;

    public TrainerWorkloadService(TrainerWorkloadRepository trainerWorkloadRepository) {
        this.trainerWorkloadRepository = trainerWorkloadRepository;
    }

    @Transactional
    public void acceptWorkload(TrainerWorkloadRequest request) {
        int year = request.getTrainingDate().getYear();
        int month = request.getTrainingDate().getMonthValue();

        TrainerWorkload existing = trainerWorkloadRepository
                .findByTrainerUsernameAndWorkYearAndWorkMonth(request.getTrainerUsername(), year, month)
                .orElse(null);

        if (request.getActionType() == ActionType.ADD) {
            TrainerWorkload workload = existing != null ? existing : createWorkload(request, year, month);
            workload.setTrainerFirstName(request.getTrainerFirstName());
            workload.setTrainerLastName(request.getTrainerLastName());
            workload.setActive(request.isActive());
            workload.setTrainingSummaryDuration(
                    workload.getTrainingSummaryDuration() + request.getTrainingDuration()
            );
            trainerWorkloadRepository.save(workload);
            log.info("Added workload for trainer={}, year={}, month={}, duration={}",
                    request.getTrainerUsername(), year, month, request.getTrainingDuration());
            return;
        }

        if (existing == null) {
            log.warn("DELETE workload ignored, no existing record for trainer={}, year={}, month={}",
                    request.getTrainerUsername(), year, month);
            return;
        }

        int updatedDuration = existing.getTrainingSummaryDuration() - request.getTrainingDuration();
        if (updatedDuration <= 0) {
            trainerWorkloadRepository.delete(existing);
            log.info("Deleted workload row for trainer={}, year={}, month={}",
                    request.getTrainerUsername(), year, month);
            return;
        }

        existing.setTrainerFirstName(request.getTrainerFirstName());
        existing.setTrainerLastName(request.getTrainerLastName());
        existing.setActive(request.isActive());
        existing.setTrainingSummaryDuration(updatedDuration);
        trainerWorkloadRepository.save(existing);

        log.info("Reduced workload for trainer={}, year={}, month={}, delta={}",
                request.getTrainerUsername(), year, month, request.getTrainingDuration());
    }

    @Transactional(readOnly = true)
    public TrainerWorkloadResponseDto getTrainerWorkload(String trainerUsername) {
        List<TrainerWorkload> workloads = trainerWorkloadRepository
                .findAllByTrainerUsernameOrderByWorkYearAscWorkMonthAsc(trainerUsername);

        if (workloads.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Trainer workload not found for username: " + trainerUsername);
        }

        return buildResponse(workloads);
    }

    @Transactional(readOnly = true)
    public TrainerWorkloadResponseDto getTrainerWorkload(String trainerUsername, int year, int month) {
        TrainerWorkload workload = trainerWorkloadRepository
                .findByTrainerUsernameAndWorkYearAndWorkMonth(trainerUsername, year, month)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Trainer workload not found for username: " + trainerUsername
                                + ", year: " + year + ", month: " + month
                ));

        return buildResponse(List.of(workload));
    }

    private TrainerWorkload createWorkload(TrainerWorkloadRequest request, int year, int month) {
        TrainerWorkload workload = new TrainerWorkload();
        workload.setTrainerUsername(request.getTrainerUsername());
        workload.setTrainerFirstName(request.getTrainerFirstName());
        workload.setTrainerLastName(request.getTrainerLastName());
        workload.setActive(request.isActive());
        workload.setWorkYear(year);
        workload.setWorkMonth(month);
        workload.setTrainingSummaryDuration(0);
        return workload;
    }

    private TrainerWorkloadResponseDto buildResponse(List<TrainerWorkload> workloads) {
        TrainerWorkload first = workloads.get(0);

        TrainerWorkloadResponseDto response = new TrainerWorkloadResponseDto();
        response.setTrainerUsername(first.getTrainerUsername());
        response.setTrainerFirstName(first.getTrainerFirstName());
        response.setTrainerLastName(first.getTrainerLastName());
        response.setTrainerStatus(first.isActive() ? "ACTIVE" : "INACTIVE");

        Map<Integer, List<TrainerWorkload>> yearsMap = workloads.stream()
                .collect(Collectors.groupingBy(
                        TrainerWorkload::getWorkYear,
                        TreeMap::new,
                        Collectors.toList()
                ));

        List<YearSummaryDto> years = new ArrayList<>();
        for (Map.Entry<Integer, List<TrainerWorkload>> yearEntry : yearsMap.entrySet()) {
            YearSummaryDto yearDto = new YearSummaryDto();
            yearDto.setYear(yearEntry.getKey());

            Map<Integer, List<TrainerWorkload>> monthsMap = yearEntry.getValue().stream()
                    .collect(Collectors.groupingBy(
                            TrainerWorkload::getWorkMonth,
                            TreeMap::new,
                            Collectors.toList()
                    ));

            List<MonthSummaryDto> months = new ArrayList<>();
            for (Map.Entry<Integer, List<TrainerWorkload>> monthEntry : monthsMap.entrySet()) {
                MonthSummaryDto monthDto = new MonthSummaryDto();
                monthDto.setMonth(monthEntry.getKey());
                monthDto.setTrainingSummaryDuration(monthEntry.getValue().get(0).getTrainingSummaryDuration());
                months.add(monthDto);
            }

            yearDto.setMonths(months);
            years.add(yearDto);
        }

        response.setYears(years);
        return response;
    }
}