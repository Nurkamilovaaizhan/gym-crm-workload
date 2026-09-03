package com.gymcrm.workload.repository;

import com.gymcrm.workload.entity.TrainerTrainingSummary;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface TrainerTrainingSummaryRepository extends MongoRepository<TrainerTrainingSummary, String> {

    Optional<TrainerTrainingSummary> findByTrainerUsername(String trainerUsername);

    List<TrainerTrainingSummary> findByTrainerFirstNameAndTrainerLastName(String trainerFirstName,
                                                                          String trainerLastName);
}