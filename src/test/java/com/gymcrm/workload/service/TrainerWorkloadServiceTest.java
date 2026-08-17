package com.gymcrm.workload.service;

import com.gymcrm.workload.constants.enums.ActionType;
import com.gymcrm.workload.dto.TrainerWorkloadRequest;
import com.gymcrm.workload.dto.TrainerWorkloadResponseDto;
import com.gymcrm.workload.entity.TrainerWorkload;
import com.gymcrm.workload.repository.TrainerWorkloadRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadServiceTest {

    @Mock
    private TrainerWorkloadRepository trainerWorkloadRepository;

    @InjectMocks
    private TrainerWorkloadService trainerWorkloadService;

    @Test
    void acceptWorkload_shouldCreateMonthlySummaryOnAdd() {
        TrainerWorkloadRequest request = request(ActionType.ADD, 30, LocalDateTime.of(2026, 8, 17, 10, 0));

        when(trainerWorkloadRepository.findByTrainerUsernameAndWorkYearAndWorkMonth("Oscar.Piastri", 2026, 8))
                .thenReturn(Optional.empty());
        when(trainerWorkloadRepository.save(any(TrainerWorkload.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        trainerWorkloadService.acceptWorkload(request);

        ArgumentCaptor<TrainerWorkload> captor = ArgumentCaptor.forClass(TrainerWorkload.class);
        verify(trainerWorkloadRepository).save(captor.capture());

        TrainerWorkload saved = captor.getValue();
        assertThat(saved.getTrainerUsername()).isEqualTo("Oscar.Piastri");
        assertThat(saved.getTrainerFirstName()).isEqualTo("Oscar");
        assertThat(saved.getTrainerLastName()).isEqualTo("Piastri");
        assertThat(saved.isActive()).isTrue();
        assertThat(saved.getWorkYear()).isEqualTo(2026);
        assertThat(saved.getWorkMonth()).isEqualTo(8);
        assertThat(saved.getTrainingSummaryDuration()).isEqualTo(30);
    }

    @Test
    void acceptWorkload_shouldDecreaseDurationOnDelete() {
        TrainerWorkload existing = workload("Oscar.Piastri", "Oscar", "Piastri", true, 2026, 8, 50);
        TrainerWorkloadRequest request = request(ActionType.DELETE, 20, LocalDateTime.of(2026, 8, 17, 10, 0));

        when(trainerWorkloadRepository.findByTrainerUsernameAndWorkYearAndWorkMonth("Oscar.Piastri", 2026, 8))
                .thenReturn(Optional.of(existing));

        trainerWorkloadService.acceptWorkload(request);

        assertThat(existing.getTrainingSummaryDuration()).isEqualTo(30);
        verify(trainerWorkloadRepository).save(existing);
        verify(trainerWorkloadRepository, never()).delete(existing);
    }

    @Test
    void acceptWorkload_shouldDeleteRowWhenDurationBecomesZeroOrNegative() {
        TrainerWorkload existing = workload("Oscar.Piastri", "Oscar", "Piastri", true, 2026, 8, 10);
        TrainerWorkloadRequest request = request(ActionType.DELETE, 20, LocalDateTime.of(2026, 8, 17, 10, 0));

        when(trainerWorkloadRepository.findByTrainerUsernameAndWorkYearAndWorkMonth("Oscar.Piastri", 2026, 8))
                .thenReturn(Optional.of(existing));

        trainerWorkloadService.acceptWorkload(request);

        verify(trainerWorkloadRepository).delete(existing);
        verify(trainerWorkloadRepository, never()).save(existing);
    }

    @Test
    void getTrainerWorkload_shouldBuildNestedSummary() {
        List<TrainerWorkload> workloads = List.of(
                workload("Oscar.Piastri", "Oscar", "Piastri", true, 2026, 8, 30),
                workload("Oscar.Piastri", "Oscar", "Piastri", true, 2026, 9, 40)
        );

        when(trainerWorkloadRepository.findAllByTrainerUsernameOrderByWorkYearAscWorkMonthAsc("Oscar.Piastri"))
                .thenReturn(workloads);

        TrainerWorkloadResponseDto response = trainerWorkloadService.getTrainerWorkload("Oscar.Piastri");

        assertThat(response.getTrainerUsername()).isEqualTo("Oscar.Piastri");
        assertThat(response.getTrainerStatus()).isEqualTo("ACTIVE");
        assertThat(response.getYears()).hasSize(1);
        assertThat(response.getYears().get(0).getYear()).isEqualTo(2026);
        assertThat(response.getYears().get(0).getMonths()).hasSize(2);
        assertThat(response.getYears().get(0).getMonths().get(0).getTrainingSummaryDuration()).isEqualTo(30);
        assertThat(response.getYears().get(0).getMonths().get(1).getTrainingSummaryDuration()).isEqualTo(40);
    }

    private TrainerWorkloadRequest request(ActionType actionType, int duration, LocalDateTime dateTime) {
        TrainerWorkloadRequest request = new TrainerWorkloadRequest();
        request.setTrainerUsername("Oscar.Piastri");
        request.setTrainerFirstName("Oscar");
        request.setTrainerLastName("Piastri");
        request.setActive(true);
        request.setTrainingDate(dateTime);
        request.setTrainingDuration(duration);
        request.setActionType(actionType);
        return request;
    }

    private TrainerWorkload workload(String username,
                                     String firstName,
                                     String lastName,
                                     boolean active,
                                     int year,
                                     int month,
                                     int duration) {
        TrainerWorkload workload = new TrainerWorkload();
        workload.setTrainerUsername(username);
        workload.setTrainerFirstName(firstName);
        workload.setTrainerLastName(lastName);
        workload.setActive(active);
        workload.setWorkYear(year);
        workload.setWorkMonth(month);
        workload.setTrainingSummaryDuration(duration);
        return workload;
    }
}