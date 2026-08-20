package com.gymcrm.workload.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "trainer_workload",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_trainer_year_month",
                columnNames = {"trainer_username", "work_year", "work_month"}
        )
)
@Getter
@Setter
public class TrainerWorkload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trainer_username", nullable = false)
    private String trainerUsername;

    @Column(name = "trainer_first_name", nullable = false)
    private String trainerFirstName;

    @Column(name = "trainer_last_name", nullable = false)
    private String trainerLastName;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "work_year", nullable = false)
    private int workYear;

    @Column(name = "work_month", nullable = false)
    private int workMonth;

    @Column(name = "training_summary_duration", nullable = false)
    private int trainingSummaryDuration;
}