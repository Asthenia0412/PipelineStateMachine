package io.github.asthenia0412.pipelinestatemachine.strategy;

import io.github.asthenia0412.pipelinestatemachine.model.enums.PhaseStatus;
import io.github.asthenia0412.pipelinestatemachine.model.task.TestTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IncrementalExecutionStrategy implements ExecutionStrategy{
    private static final Logger logger = LoggerFactory.getLogger(IncrementalExecutionStrategy.class);


    @Override
    public void execute(TestTask task) {
        logger.info("执行 增量执行操作 针对task：{}", task.getTaskId());
        task.setPhaseStatus(PhaseStatus.IN_PROCESSING);
        task.setPhaseStatus(PhaseStatus.SUCCESS);
    }


}
