package io.github.asthenia0412.pipelinestatemachine.strategy;

import io.github.asthenia0412.pipelinestatemachine.model.task.TestTask;

public interface ExecutionStrategy {
    void execute(TestTask task);
}
