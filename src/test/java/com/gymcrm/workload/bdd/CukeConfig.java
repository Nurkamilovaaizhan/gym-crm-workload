package com.gymcrm.workload.bdd;

import com.gymcrm.workload.GymCrmWorkloadApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import io.cucumber.spring.CucumberContextConfiguration;
import org.testcontainers.containers.MongoDBContainer;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@CucumberContextConfiguration
@SpringBootTest(
        classes = GymCrmWorkloadApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CukeConfig {

    private static final MongoDBContainer MONGO =
            new MongoDBContainer("mongo:7.0");

    static {
        MONGO.start();
    }

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.data.mongodb.uri",
                MONGO::getReplicaSetUrl
        );
    }
}