package com.gymcrm.workload.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymcrm.workload.dto.TrainerWorkloadRequest;
import com.gymcrm.workload.service.TrainerWorkloadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TrainerWorkloadListener {

    private final ObjectMapper objectMapper;
    private final TrainerWorkloadService trainerWorkloadService;

    public TrainerWorkloadListener(ObjectMapper objectMapper,
                                   TrainerWorkloadService trainerWorkloadService) {
        this.objectMapper = objectMapper;
        this.trainerWorkloadService = trainerWorkloadService;
    }

    @JmsListener(destination = "${app.jms.workload-queue}")
    public void receive(String payload,
                        @Header(name = "X-Transaction-Id", required = false) String transactionId) {
        try {
            TrainerWorkloadRequest request = objectMapper.readValue(payload, TrainerWorkloadRequest.class);

            log.info("Workload message received, transactionId={}, trainer={}, action={}",
                    transactionId, request.getTrainerUsername(), request.getActionType());

            trainerWorkloadService.acceptWorkload(request, transactionId);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Invalid workload message format", ex);
        }
    }
}