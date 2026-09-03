package com.gymcrm.workload.service;

import com.gymcrm.workload.constants.enums.ActionType;
import com.gymcrm.workload.dto.MonthSummaryDto;
import com.gymcrm.workload.dto.TrainerWorkloadRequest;
import com.gymcrm.workload.dto.TrainerWorkloadResponseDto;
import com.gymcrm.workload.dto.YearSummaryDto;
import com.gymcrm.workload.entity.TrainerTrainingSummary;
import com.gymcrm.workload.repository.TrainerTrainingSummaryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
public class TrainerWorkloadService {

    private final TrainerTrainingSummaryRepository trainerTrainingSummaryRepository;

    public TrainerWorkloadService(TrainerTrainingSummaryRepository trainerTrainingSummaryRepository) {
        this.trainerTrainingSummaryRepository = trainerTrainingSummaryRepository;
    }

    public void acceptWorkload(TrainerWorkloadRequest request) {
        acceptWorkload(request, null);
    }

    public void acceptWorkload(TrainerWorkloadRequest request, String transactionId) {
        validateRequest(request);

        int year = request.getTrainingDate().getYear();
        int month = request.getTrainingDate().getMonthValue();

        TrainerTrainingSummary summary = trainerTrainingSummaryRepository
                .findByTrainerUsername(request.getTrainerUsername())
                .orElseGet(() -> createSummary(request));

        summary.setTrainerFirstName(request.getTrainerFirstName());
        summary.setTrainerLastName(request.getTrainerLastName());
        summary.setTrainerStatus(request.isActive());

        TrainerTrainingSummary.YearSummary yearSummary = findOrCreateYear(summary, year);
        TrainerTrainingSummary.MonthSummary monthSummary = findOrCreateMonth(yearSummary, month);

        int currentDuration = monthSummary.getTrainingSummaryDuration();

        if (request.getActionType() == ActionType.ADD) {
            monthSummary.setTrainingSummaryDuration(currentDuration + request.getTrainingDuration());
            log.info("transactionId={}, added workload for trainer={}, year={}, month={}, duration={}",
                    transactionId, request.getTrainerUsername(), year, month, request.getTrainingDuration());
        } else {
            int updatedDuration = Math.max(0, currentDuration - request.getTrainingDuration());
            monthSummary.setTrainingSummaryDuration(updatedDuration);
            log.info("transactionId={}, reduced workload for trainer={}, year={}, month={}, duration={}",
                    transactionId, request.getTrainerUsername(), year, month, request.getTrainingDuration());
        }

        trainerTrainingSummaryRepository.save(summary);

        log.info("transactionId={}, saved trainer workload summary, trainer={}, year={}, month={}, totalDuration={}",
                transactionId,
                request.getTrainerUsername(),
                year,
                month,
                monthSummary.getTrainingSummaryDuration());
    }

    public TrainerWorkloadResponseDto getTrainerWorkload(String trainerUsername) {
        TrainerTrainingSummary summary = trainerTrainingSummaryRepository.findByTrainerUsername(trainerUsername)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Trainer workload not found for username: " + trainerUsername
                ));

        return toResponse(summary);
    }

    public TrainerWorkloadResponseDto getTrainerWorkload(String trainerUsername, int year, int month) {
        TrainerTrainingSummary summary = trainerTrainingSummaryRepository.findByTrainerUsername(trainerUsername)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Trainer workload not found for username: " + trainerUsername
                ));

        TrainerTrainingSummary.YearSummary yearSummary = summary.getYears().stream()
                .filter(item -> item.getYear() == year)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Trainer workload not found for username: " + trainerUsername
                                + ", year: " + year
                ));

        TrainerTrainingSummary.MonthSummary monthSummary = yearSummary.getMonths().stream()
                .filter(item -> item.getMonth() == month)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Trainer workload not found for username: " + trainerUsername
                                + ", year: " + year
                                + ", month: " + month
                ));

        TrainerTrainingSummary filtered = new TrainerTrainingSummary();
        filtered.setTrainerUsername(summary.getTrainerUsername());
        filtered.setTrainerFirstName(summary.getTrainerFirstName());
        filtered.setTrainerLastName(summary.getTrainerLastName());
        filtered.setTrainerStatus(summary.isTrainerStatus());

        TrainerTrainingSummary.YearSummary filteredYear = new TrainerTrainingSummary.YearSummary();
        filteredYear.setYear(year);
        filteredYear.setMonths(List.of(monthSummary));

        filtered.setYears(List.of(filteredYear));

        return toResponse(filtered);
    }

    private TrainerTrainingSummary createSummary(TrainerWorkloadRequest request) {
        TrainerTrainingSummary summary = new TrainerTrainingSummary();
        summary.setTrainerUsername(request.getTrainerUsername());
        summary.setTrainerFirstName(request.getTrainerFirstName());
        summary.setTrainerLastName(request.getTrainerLastName());
        summary.setTrainerStatus(request.isActive());
        return summary;
    }

    private TrainerTrainingSummary.YearSummary findOrCreateYear(TrainerTrainingSummary summary, int year) {
        return summary.getYears().stream()
                .filter(item -> item.getYear() == year)
                .findFirst()
                .orElseGet(() -> {
                    TrainerTrainingSummary.YearSummary yearSummary = new TrainerTrainingSummary.YearSummary();
                    yearSummary.setYear(year);
                    summary.getYears().add(yearSummary);
                    return yearSummary;
                });
    }

    private TrainerTrainingSummary.MonthSummary findOrCreateMonth(TrainerTrainingSummary.YearSummary yearSummary,
                                                                  int month) {
        return yearSummary.getMonths().stream()
                .filter(item -> item.getMonth() == month)
                .findFirst()
                .orElseGet(() -> {
                    TrainerTrainingSummary.MonthSummary monthSummary = new TrainerTrainingSummary.MonthSummary();
                    monthSummary.setMonth(month);
                    monthSummary.setTrainingSummaryDuration(0);
                    yearSummary.getMonths().add(monthSummary);
                    return monthSummary;
                });
    }

    private TrainerWorkloadResponseDto toResponse(TrainerTrainingSummary summary) {
        TrainerWorkloadResponseDto response = new TrainerWorkloadResponseDto();
        response.setTrainerUsername(summary.getTrainerUsername());
        response.setTrainerFirstName(summary.getTrainerFirstName());
        response.setTrainerLastName(summary.getTrainerLastName());
        response.setTrainerStatus(summary.isTrainerStatus());

        List<YearSummaryDto> years = summary.getYears().stream()
                .sorted(Comparator.comparingInt(TrainerTrainingSummary.YearSummary::getYear))
                .map(yearSummary -> {
                    YearSummaryDto yearDto = new YearSummaryDto();
                    yearDto.setYear(yearSummary.getYear());

                    List<MonthSummaryDto> months = yearSummary.getMonths().stream()
                            .sorted(Comparator.comparingInt(TrainerTrainingSummary.MonthSummary::getMonth))
                            .map(monthSummary -> {
                                MonthSummaryDto monthDto = new MonthSummaryDto();
                                monthDto.setMonth(monthSummary.getMonth());
                                monthDto.setTrainingSummaryDuration(monthSummary.getTrainingSummaryDuration());
                                return monthDto;
                            })
                            .toList();

                    yearDto.setMonths(months);
                    return yearDto;
                })
                .toList();

        response.setYears(years);
        return response;
    }

    private void validateRequest(TrainerWorkloadRequest request) {
        if (request.getTrainerUsername() == null || request.getTrainerUsername().isBlank()) {
            throw new IllegalArgumentException("Trainer username is required");
        }
        if (request.getTrainerFirstName() == null || request.getTrainerFirstName().isBlank()) {
            throw new IllegalArgumentException("Trainer first name is required");
        }
        if (request.getTrainerLastName() == null || request.getTrainerLastName().isBlank()) {
            throw new IllegalArgumentException("Trainer last name is required");
        }
        if (request.getTrainingDate() == null) {
            throw new IllegalArgumentException("Training date is required");
        }
        if (request.getTrainingDuration() <= 0) {
            throw new IllegalArgumentException("Training duration must be greater than zero");
        }
        if (request.getActionType() == null) {
            throw new IllegalArgumentException("Action type is required");
        }
    }
}