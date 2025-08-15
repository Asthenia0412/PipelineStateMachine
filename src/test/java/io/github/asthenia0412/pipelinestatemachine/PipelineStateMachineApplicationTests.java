package io.github.asthenia0412.pipelinestatemachine;

import io.github.asthenia0412.pipelinestatemachine.mapper.TestTaskMapper;
import io.github.asthenia0412.pipelinestatemachine.model.enums.ExecutionPhase;
import io.github.asthenia0412.pipelinestatemachine.model.enums.PhaseStatus;
import io.github.asthenia0412.pipelinestatemachine.model.enums.TaskStatus;
import io.github.asthenia0412.pipelinestatemachine.model.task.TestTask;
import io.github.asthenia0412.pipelinestatemachine.service.TestPipelineService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
class PipelineStateMachineApplicationTests {

    @Autowired
    private TestPipelineService pipelineService;
    @Autowired
    private TestTaskMapper taskMapper;

    @Test
    void testFullExecution() {
        TestTask task = new TestTask();
        task.setTaskId("task1");
        task.setTestCaseId("test1");
        task.setExecutionMode("FULL");
        task.setTaskStatus(TaskStatus.PENDING);
        task.setCurrentPhase(ExecutionPhase.COMPILE);
        task.setPhaseStatus(PhaseStatus.NOT_STARTED);

        pipelineService.submitTask(task);
        sleep(15000); // 等待调度
        TestTask updated = taskMapper.findByTaskId("task1");
        assertNull(updated); // 已完成，任务被删除
    }

    @Test
    void testIncrementalExecution() {
        TestTask task = new TestTask();
        task.setTaskId("task2");
        task.setTestCaseId("test2");
        task.setExecutionMode("INCREMENTAL");
        task.setTaskStatus(TaskStatus.PENDING);
        task.setCurrentPhase(ExecutionPhase.COMPILE);
        task.setPhaseStatus(PhaseStatus.NOT_STARTED);

        pipelineService.submitTask(task);
        sleep(15000);
        assertNull(taskMapper.findByTaskId("task2"));
    }

    @Test
    void testConcurrentTasks() {
        for (int i = 0; i < 10; i++) {
            TestTask task = new TestTask();
            task.setTaskId("task" + i);
            task.setTestCaseId("test" + i);
            task.setExecutionMode("FULL");
            task.setTaskStatus(TaskStatus.PENDING);
            task.setCurrentPhase(ExecutionPhase.COMPILE);
            task.setPhaseStatus(PhaseStatus.NOT_STARTED);
            pipelineService.submitTask(task);
        }
        sleep(20000);
        for (int i = 0; i < 10; i++) {
            assertNull(taskMapper.findByTaskId("task" + i));
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
