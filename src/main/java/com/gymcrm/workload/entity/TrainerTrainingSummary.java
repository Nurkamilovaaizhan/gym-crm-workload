package com.gymcrm.workload.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Document(collection = "trainer_training_summary")
@CompoundIndex(
        name = "idx_trainer_first_last_name",
        def = "{'trainerFirstName': 1, 'trainerLastName': 1}"
)
public class TrainerTrainingSummary {

    @Id
    private String id;

    private String trainerUsername;
    private String trainerFirstName;
    private String trainerLastName;
    private boolean trainerStatus;

    private List<YearSummary> years = new ArrayList<>();

    @Getter
    @Setter
    public static class YearSummary {
        private int year;
        private List<MonthSummary> months = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class MonthSummary {
        private int month;
        private int trainingSummaryDuration;
    }
}