package com.gymcrm.workload.service;

import com.gymcrm.workload.constants.enums.ActionType;
import com.gymcrm.workload.dto.TrainerWorkloadRequest;
import com.gymcrm.workload.dto.TrainerWorkloadResponseDto;
import com.gymcrm.workload.entity.TrainerTrainingSummary;
import com.gymcrm.workload.repository.TrainerTrainingSummaryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadServiceTest {

    @Mock
    private TrainerTrainingSummaryRepository trainerTrainingSummaryRepository;

    @InjectMocks
    private TrainerWorkloadService trainerWorkloadService;

    @Test
    void acceptWorkload_shouldCreateDocumentWithYearAndMonthWhenTrainerDoesNotExist() {
        TrainerWorkloadRequest request = request(ActionType.ADD, 30, LocalDateTime.of(2026, 8, 17, 10, 0));

        when(trainerTrainingSummaryRepository.findByTrainerUsername("Oscar.Piastri"))
                .thenReturn(Optional.empty());
        when(trainerTrainingSummaryRepository.save(any(TrainerTrainingSummary.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        trainerWorkloadService.acceptWorkload(request, "tx-1");

        ArgumentCaptor<TrainerTrainingSummary> captor = ArgumentCaptor.forClass(TrainerTrainingSummary.class);
        verify(trainerTrainingSummaryRepository).save(captor.capture());

        TrainerTrainingSummary saved = captor.getValue();

        assertThat(saved.getTrainerUsername()).isEqualTo("Oscar.Piastri");
        assertThat(saved.getTrainerFirstName()).isEqualTo("Oscar");
        assertThat(saved.getTrainerLastName()).isEqualTo("Piastri");
        assertThat(saved.isTrainerStatus()).isTrue();

        assertThat(saved.getYears()).hasSize(1);
        assertThat(saved.getYears().get(0).getYear()).isEqualTo(2026);

        assertThat(saved.getYears().get(0).getMonths()).hasSize(1);
        assertThat(saved.getYears().get(0).getMonths().get(0).getMonth()).isEqualTo(8);
        assertThat(saved.getYears().get(0).getMonths().get(0).getTrainingSummaryDuration()).isEqualTo(30);
    }

    @Test
    void acceptWorkload_shouldAddDurationToExistingMonth() {
        TrainerTrainingSummary existing = summary("Oscar.Piastri", "Oscar", "Piastri", true);
        TrainerTrainingSummary.YearSummary year = year(2026);
        year.getMonths().add(month(8, 50));
        existing.getYears().add(year);

        TrainerWorkloadRequest request = request(ActionType.ADD, 30, LocalDateTime.of(2026, 8, 17, 10, 0));

        when(trainerTrainingSummaryRepository.findByTrainerUsername("Oscar.Piastri"))
                .thenReturn(Optional.of(existing));
        when(trainerTrainingSummaryRepository.save(any(TrainerTrainingSummary.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        trainerWorkloadService.acceptWorkload(request, "tx-2");

        assertThat(existing.getYears().get(0).getMonths().get(0).getTrainingSummaryDuration())
                .isEqualTo(80);

        verify(trainerTrainingSummaryRepository).save(existing);
    }

    @Test
    void acceptWorkload_shouldCreateNewMonthWhenMonthDoesNotExist() {
        TrainerTrainingSummary existing = summary("Oscar.Piastri", "Oscar", "Piastri", true);
        TrainerTrainingSummary.YearSummary year = year(2026);
        year.getMonths().add(month(7, 40));
        existing.getYears().add(year);

        TrainerWorkloadRequest request = request(ActionType.ADD, 20, LocalDateTime.of(2026, 8, 17, 10, 0));

        when(trainerTrainingSummaryRepository.findByTrainerUsername("Oscar.Piastri"))
                .thenReturn(Optional.of(existing));
        when(trainerTrainingSummaryRepository.save(any(TrainerTrainingSummary.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        trainerWorkloadService.acceptWorkload(request, "tx-3");

        assertThat(existing.getYears().get(0).getMonths()).hasSize(2);
        assertThat(existing.getYears().get(0).getMonths().get(1).getMonth()).isEqualTo(8);
        assertThat(existing.getYears().get(0).getMonths().get(1).getTrainingSummaryDuration()).isEqualTo(20);

        verify(trainerTrainingSummaryRepository).save(existing);
    }

    @Test
    void acceptWorkload_shouldCreateNewYearWhenYearDoesNotExist() {
        TrainerTrainingSummary existing = summary("Oscar.Piastri", "Oscar", "Piastri", true);
        TrainerTrainingSummary.YearSummary oldYear = year(2025);
        oldYear.getMonths().add(month(12, 90));
        existing.getYears().add(oldYear);

        TrainerWorkloadRequest request = request(ActionType.ADD, 45, LocalDateTime.of(2026, 1, 10, 10, 0));

        when(trainerTrainingSummaryRepository.findByTrainerUsername("Oscar.Piastri"))
                .thenReturn(Optional.of(existing));
        when(trainerTrainingSummaryRepository.save(any(TrainerTrainingSummary.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        trainerWorkloadService.acceptWorkload(request, "tx-4");

        assertThat(existing.getYears()).hasSize(2);
        assertThat(existing.getYears().get(1).getYear()).isEqualTo(2026);
        assertThat(existing.getYears().get(1).getMonths().get(0).getMonth()).isEqualTo(1);
        assertThat(existing.getYears().get(1).getMonths().get(0).getTrainingSummaryDuration()).isEqualTo(45);

        verify(trainerTrainingSummaryRepository).save(existing);
    }

    @Test
    void acceptWorkload_shouldDecreaseDurationOnDelete() {
        TrainerTrainingSummary existing = summary("Oscar.Piastri", "Oscar", "Piastri", true);
        TrainerTrainingSummary.YearSummary year = year(2026);
        year.getMonths().add(month(8, 50));
        existing.getYears().add(year);

        TrainerWorkloadRequest request = request(ActionType.DELETE, 20, LocalDateTime.of(2026, 8, 17, 10, 0));

        when(trainerTrainingSummaryRepository.findByTrainerUsername("Oscar.Piastri"))
                .thenReturn(Optional.of(existing));
        when(trainerTrainingSummaryRepository.save(any(TrainerTrainingSummary.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        trainerWorkloadService.acceptWorkload(request, "tx-5");

        assertThat(existing.getYears().get(0).getMonths().get(0).getTrainingSummaryDuration())
                .isEqualTo(30);

        verify(trainerTrainingSummaryRepository).save(existing);
    }

    @Test
    void acceptWorkload_shouldNotMakeDurationNegativeOnDelete() {
        TrainerTrainingSummary existing = summary("Oscar.Piastri", "Oscar", "Piastri", true);
        TrainerTrainingSummary.YearSummary year = year(2026);
        year.getMonths().add(month(8, 10));
        existing.getYears().add(year);

        TrainerWorkloadRequest request = request(ActionType.DELETE, 20, LocalDateTime.of(2026, 8, 17, 10, 0));

        when(trainerTrainingSummaryRepository.findByTrainerUsername("Oscar.Piastri"))
                .thenReturn(Optional.of(existing));
        when(trainerTrainingSummaryRepository.save(any(TrainerTrainingSummary.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        trainerWorkloadService.acceptWorkload(request, "tx-6");

        assertThat(existing.getYears().get(0).getMonths().get(0).getTrainingSummaryDuration())
                .isZero();

        verify(trainerTrainingSummaryRepository).save(existing);
    }

    @Test
    void getTrainerWorkload_shouldReturnNestedSummary() {
        TrainerTrainingSummary existing = summary("Oscar.Piastri", "Oscar", "Piastri", true);

        TrainerTrainingSummary.YearSummary year2026 = year(2026);
        year2026.getMonths().add(month(8, 80));
        year2026.getMonths().add(month(9, 40));
        existing.getYears().add(year2026);

        when(trainerTrainingSummaryRepository.findByTrainerUsername("Oscar.Piastri"))
                .thenReturn(Optional.of(existing));

        TrainerWorkloadResponseDto response = trainerWorkloadService.getTrainerWorkload("Oscar.Piastri");

        assertThat(response.getTrainerUsername()).isEqualTo("Oscar.Piastri");
        assertThat(response.getTrainerFirstName()).isEqualTo("Oscar");
        assertThat(response.getTrainerLastName()).isEqualTo("Piastri");
        assertThat(response.isTrainerStatus()).isTrue();

        assertThat(response.getYears()).hasSize(1);
        assertThat(response.getYears().get(0).getYear()).isEqualTo(2026);
        assertThat(response.getYears().get(0).getMonths()).hasSize(2);
        assertThat(response.getYears().get(0).getMonths().get(0).getTrainingSummaryDuration()).isEqualTo(80);
        assertThat(response.getYears().get(0).getMonths().get(1).getTrainingSummaryDuration()).isEqualTo(40);
    }

    @Test
    void getTrainerWorkloadForMonth_shouldReturnOnlyRequestedMonth() {
        TrainerTrainingSummary existing = summary("Oscar.Piastri", "Oscar", "Piastri", true);

        TrainerTrainingSummary.YearSummary year2026 = year(2026);
        year2026.getMonths().add(month(8, 80));
        year2026.getMonths().add(month(9, 40));
        existing.getYears().add(year2026);

        when(trainerTrainingSummaryRepository.findByTrainerUsername("Oscar.Piastri"))
                .thenReturn(Optional.of(existing));

        TrainerWorkloadResponseDto response = trainerWorkloadService.getTrainerWorkload("Oscar.Piastri", 2026, 9);

        assertThat(response.getYears()).hasSize(1);
        assertThat(response.getYears().get(0).getYear()).isEqualTo(2026);
        assertThat(response.getYears().get(0).getMonths()).hasSize(1);
        assertThat(response.getYears().get(0).getMonths().get(0).getMonth()).isEqualTo(9);
        assertThat(response.getYears().get(0).getMonths().get(0).getTrainingSummaryDuration()).isEqualTo(40);
    }

    @Test
    void getTrainerWorkload_shouldThrowWhenTrainerNotFound() {
        when(trainerTrainingSummaryRepository.findByTrainerUsername("Unknown.User"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerWorkloadService.getTrainerWorkload("Unknown.User"))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void acceptWorkload_shouldThrowWhenRequiredFieldsMissing() {
        TrainerWorkloadRequest request = new TrainerWorkloadRequest();

        assertThatThrownBy(() -> trainerWorkloadService.acceptWorkload(request, "tx-7"))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(trainerTrainingSummaryRepository);
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

    private TrainerTrainingSummary summary(String username,
                                           String firstName,
                                           String lastName,
                                           boolean status) {
        TrainerTrainingSummary summary = new TrainerTrainingSummary();
        summary.setTrainerUsername(username);
        summary.setTrainerFirstName(firstName);
        summary.setTrainerLastName(lastName);
        summary.setTrainerStatus(status);
        return summary;
    }

    private TrainerTrainingSummary.YearSummary year(int year) {
        TrainerTrainingSummary.YearSummary yearSummary = new TrainerTrainingSummary.YearSummary();
        yearSummary.setYear(year);
        return yearSummary;
    }

    private TrainerTrainingSummary.MonthSummary month(int month, int duration) {
        TrainerTrainingSummary.MonthSummary monthSummary = new TrainerTrainingSummary.MonthSummary();
        monthSummary.setMonth(month);
        monthSummary.setTrainingSummaryDuration(duration);
        return monthSummary;
    }
}