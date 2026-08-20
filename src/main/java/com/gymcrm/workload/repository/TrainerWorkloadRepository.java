package com.gymcrm.workload.repository;

import com.gymcrm.workload.entity.TrainerWorkload;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TrainerWorkloadRepository extends JpaRepository<TrainerWorkload, Long> {

    Optional<TrainerWorkload> findByTrainerUsernameAndWorkYearAndWorkMonth(String trainerUsername,
                                                                           int workYear,
                                                                           int workMonth);

    List<TrainerWorkload> findAllByTrainerUsernameOrderByWorkYearAscWorkMonthAsc(String trainerUsername);
}