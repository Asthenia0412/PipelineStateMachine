package io.github.asthenia0412.pipelinestatemachine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PipelineStateMachineApplication {

    public static void main(String[] args) {
        SpringApplication.run(PipelineStateMachineApplication.class, args);
    }

}
