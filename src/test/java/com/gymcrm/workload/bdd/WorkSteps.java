package com.gymcrm.workload.bdd;

import com.gymcrm.workload.constants.enums.ActionType;
import com.gymcrm.workload.dto.TrainerWorkloadRequest;
import com.gymcrm.workload.entity.TrainerTrainingSummary;
import com.gymcrm.workload.repository.TrainerTrainingSummaryRepository;
import com.gymcrm.workload.service.TrainerWorkloadService;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class WorkSteps {

    private TrainerWorkloadService service;
    private TrainerTrainingSummaryRepository repository;
    private TrainerTrainingSummary saved;
    private Exception error;

    @Before
    public void setUp() {
        repository = mock(TrainerTrainingSummaryRepository.class);

        when(repository.findByTrainerUsername("Trainer.One"))
                .thenReturn(Optional.empty());

        when(repository.save(any(TrainerTrainingSummary.class)))
                .thenAnswer(invocation -> {
                    saved = invocation.getArgument(0);
                    return saved;
                });

        service = new TrainerWorkloadService(repository);
    }

    @Given("workload component is ready")
    public void workloadReady() {
        error = null;
        saved = null;
    }

    @When("I add {int} minutes for trainer")
    public void addMinutes(int duration) {
        TrainerWorkloadRequest request = new TrainerWorkloadRequest();
        request.setTrainerUsername("Trainer.One");
        request.setTrainerFirstName("Trainer");
        request.setTrainerLastName("One");
        request.setActive(true);
        request.setTrainingDate(LocalDateTime.of(2026, 9, 10, 10, 0));
        request.setTrainingDuration(duration);
        request.setActionType(ActionType.ADD);

        try {
            service.acceptWorkload(request, "test-transaction");
        } catch (Exception exception) {
            error = exception;
        }
    }

    @When("I add invalid workload")
    public void addInvalidWorkload() {
        TrainerWorkloadRequest request = new TrainerWorkloadRequest();

        try {
            service.acceptWorkload(request, "test-transaction");
        } catch (Exception exception) {
            error = exception;
        }
    }

    @Then("workload duration is {int} minutes")
    public void workloadDuration(int expected) {
        assertThat(error).isNull();
        assertThat(saved).isNotNull();
        assertThat(saved.getYears()).hasSize(1);
        assertThat(saved.getYears().get(0).getMonths()).hasSize(1);
        assertThat(saved.getYears()
                .get(0)
                .getMonths()
                .get(0)
                .getTrainingSummaryDuration())
                .isEqualTo(expected);
    }

    @Then("workload validation error is returned")
    public void validationError() {
        assertThat(error).isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(repository);
    }
}