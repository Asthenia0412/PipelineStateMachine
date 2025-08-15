package io.github.asthenia0412.pipelinestatemachine.strategy;

import io.github.asthenia0412.pipelinestatemachine.model.enums.PhaseStatus;
import io.github.asthenia0412.pipelinestatemachine.model.task.TestTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomExecutionStrategy implements  ExecutionStrategy{
    private static final Logger logger = LoggerFactory.getLogger(CustomExecutionStrategy.class);
    private final String customTestCaseId;

    public CustomExecutionStrategy(String customTestCaseId) {
        this.customTestCaseId = customTestCaseId;
    }

    @Override
    public void execute(TestTask task) {
        logger.info("执行自定义任务流程 针对task :{}",task.getTaskId());
        task.setPhaseStatus(PhaseStatus.IN_PROCESSING);
        task.setPhaseStatus(PhaseStatus.SUCCESS);
    }
}
