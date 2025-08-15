package io.github.asthenia0412.pipelinestatemachine.model.task;

import io.github.asthenia0412.pipelinestatemachine.model.enums.ExecutionPhase;
import io.github.asthenia0412.pipelinestatemachine.model.enums.PhaseStatus;
import io.github.asthenia0412.pipelinestatemachine.model.enums.TaskStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TestTask {
    private String taskId;
    private String testCaseId;
    private String executionMode;
    private TaskStatus taskStatus;
    private ExecutionPhase currentPhase;
    private PhaseStatus phaseStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
