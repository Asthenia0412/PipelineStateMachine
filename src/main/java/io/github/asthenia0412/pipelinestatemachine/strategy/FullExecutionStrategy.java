package io.github.asthenia0412.pipelinestatemachine.strategy;

import io.github.asthenia0412.pipelinestatemachine.model.enums.PhaseStatus;
import io.github.asthenia0412.pipelinestatemachine.model.task.TestTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FullExecutionStrategy implements ExecutionStrategy{
    private static final Logger logger = LoggerFactory.getLogger(FullExecutionStrategy.class);

    @Override
    public void execute(TestTask task) {
        logger.info("针对task执行全量操作，task为：{}",task.getTaskId());
        task.setPhaseStatus(PhaseStatus.IN_PROCESSING);
        // 模拟执行
        task.setPhaseStatus(PhaseStatus.SUCCESS);
    }


}
