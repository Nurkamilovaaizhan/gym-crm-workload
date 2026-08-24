package com.gymcrm.workload;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.jms.annotation.EnableJms;

@EnableJms
@SpringBootApplication
@EnableDiscoveryClient
public class GymCrmWorkloadApplication {

    public static void main(String[] args) {
        SpringApplication.run(GymCrmWorkloadApplication.class, args);
    }
}