package io.github.asthenia0412.pipelinestatemachine.service;

import io.github.asthenia0412.pipelinestatemachine.mapper.TestTaskMapper;
import io.github.asthenia0412.pipelinestatemachine.model.enums.ExecutionPhase;
import io.github.asthenia0412.pipelinestatemachine.model.enums.PhaseStatus;
import io.github.asthenia0412.pipelinestatemachine.model.enums.TaskStatus;
import io.github.asthenia0412.pipelinestatemachine.model.task.TestTask;
import io.github.asthenia0412.pipelinestatemachine.strategy.ExecutionStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StateMachineEngine {
    @Autowired
    TestTaskMapper testTaskMapper;

    private static final Logger logger = LoggerFactory.getLogger(StateMachineEngine.class);

    public void transition(TestTask task, ExecutionPhase nextPhase) {
        logger.info("Task阶段转换发生: {} 从阶段 phase: {} 阶段到 {}", task.getTaskId(), task.getCurrentPhase(), nextPhase);
        switch (nextPhase) {
            case COMPILE:
                task.setTaskStatus(TaskStatus.RUNNING);
                task.setCurrentPhase(ExecutionPhase.COMPILE);
                task.setPhaseStatus(PhaseStatus.IN_PROCESSING);
                task.setPhaseStatus(PhaseStatus.SUCCESS);
                testTaskMapper.updateTask(task);
                break;
            case EXECUTE:
                task.setTaskStatus(TaskStatus.RUNNING);
                task.setCurrentPhase(ExecutionPhase.EXECUTE);
                task.setPhaseStatus(PhaseStatus.IN_PROCESSING);
                testTaskMapper.updateTask(task);
                break;
            case VALIDATE:
                task.setTaskStatus(TaskStatus.RUNNING);
                task.setCurrentPhase(ExecutionPhase.VALIDATE);
                task.setPhaseStatus(PhaseStatus.SUCCESS);
                task.setTaskStatus(TaskStatus.COMPLETED);
                testTaskMapper.updateTask(task);
                break;
            default:
                logger.error("无效的阶段转换: {}", task.getTaskId());
                task.setTaskStatus(TaskStatus.FAILED);
                task.setPhaseStatus(PhaseStatus.ERROR);
                testTaskMapper.updateTask(task);
        }
    }
}
