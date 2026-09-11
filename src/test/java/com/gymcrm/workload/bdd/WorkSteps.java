package com.gymcrm.workload.bdd;

import com.gymcrm.workload.entity.TrainerTrainingSummary;
import com.gymcrm.workload.repository.TrainerTrainingSummaryRepository;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class WorkSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TrainerTrainingSummaryRepository repository;

    private Exception error;

    @Before
    public void cleanDatabase() {
        repository.deleteAll();
        error = null;
    }

    @Given("workload component is ready")
    public void workloadReady() {
        TrainerTrainingSummary summary = new TrainerTrainingSummary();
        summary.setTrainerUsername("Trainer.One");
        summary.setTrainerFirstName("Trainer");
        summary.setTrainerLastName("One");
        summary.setTrainerStatus(true);

        TrainerTrainingSummary.YearSummary year =
                new TrainerTrainingSummary.YearSummary();
        year.setYear(2026);

        TrainerTrainingSummary.MonthSummary month =
                new TrainerTrainingSummary.MonthSummary();
        month.setMonth(9);
        month.setTrainingSummaryDuration(60);

        year.getMonths().add(month);
        summary.getYears().add(year);

        repository.save(summary);
    }

    @When("I request workload for trainer")
    public void requestWorkload() {
        try {
            mockMvc.perform(
                    get("/api/workload/Trainer.One")
                            .with(user("test-user"))
            ).andExpect(status().isOk());
        } catch (Exception exception) {
            error = exception;
        }
    }

    @When("I request workload for unknown trainer")
    public void requestUnknownWorkload() {
        try {
            mockMvc.perform(
                    get("/api/workload/Unknown.User")
                            .with(user("test-user"))
            ).andExpect(status().isNotFound());
        } catch (Exception exception) {
            error = exception;
        }
    }

    @Then("workload response contains {int} minutes")
    public void workloadContains(int expectedDuration) {
        assertThat(error).isNull();

        try {
            mockMvc.perform(
                            get("/api/workload/Trainer.One")
                                    .with(user("test-user"))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.trainerUsername")
                            .value("Trainer.One"))
                    .andExpect(jsonPath("$.years[0].months[0].trainingSummaryDuration")
                            .value(expectedDuration));
        } catch (Exception exception) {
            throw new AssertionError("Workload response is invalid", exception);
        }
    }

    @Then("workload request returns not found")
    public void workloadNotFound() {
        assertThat(error).isNull();
    }
}