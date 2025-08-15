package io.github.asthenia0412.pipelinestatemachine.service;

import io.github.asthenia0412.pipelinestatemachine.model.enums.ExecutionPhase;
import io.github.asthenia0412.pipelinestatemachine.model.enums.PhaseStatus;
import io.github.asthenia0412.pipelinestatemachine.model.enums.TaskStatus;
import io.github.asthenia0412.pipelinestatemachine.model.task.TestTask;
import io.github.asthenia0412.pipelinestatemachine.strategy.ExecutionStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class StateMachineEngine {
    private static final Logger logger = LoggerFactory.getLogger(StateMachineEngine.class);

    public void transition(TestTask task, ExecutionPhase nextPhase) {
        logger.debug("Transitioning task: {} from phase: {} to {}", task.getTaskId(), task.getCurrentPhase(), nextPhase);
        switch (nextPhase) {
            case COMPILE:
                task.setTaskStatus(TaskStatus.RUNNING);
                task.setCurrentPhase(ExecutionPhase.COMPILE);
                task.setPhaseStatus(PhaseStatus.IN_PROCESSING);
                task.setPhaseStatus(PhaseStatus.SUCCESS);
                break;
            case EXECUTE:
                task.setCurrentPhase(ExecutionPhase.EXECUTE);
                task.setPhaseStatus(PhaseStatus.IN_PROCESSING);
                break;
            case VALIDATE:
                task.setCurrentPhase(ExecutionPhase.VALIDATE);
                task.setPhaseStatus(PhaseStatus.SUCCESS);
                task.setTaskStatus(TaskStatus.COMPLETED);
                break;
            default:
                logger.error("Invalid phase transition for task: {}", task.getTaskId());
                task.setTaskStatus(TaskStatus.FAILED);
                task.setPhaseStatus(PhaseStatus.ERROR);
        }
    }
}
