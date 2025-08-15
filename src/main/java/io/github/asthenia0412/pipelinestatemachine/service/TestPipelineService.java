package io.github.asthenia0412.pipelinestatemachine.service;

import io.github.asthenia0412.pipelinestatemachine.mapper.TestTaskMapper;
import io.github.asthenia0412.pipelinestatemachine.model.enums.ExecutionPhase;
import io.github.asthenia0412.pipelinestatemachine.model.enums.PhaseStatus;
import io.github.asthenia0412.pipelinestatemachine.model.enums.TaskStatus;
import io.github.asthenia0412.pipelinestatemachine.model.task.TestTask;
import io.github.asthenia0412.pipelinestatemachine.strategy.CustomExecutionStrategy;
import io.github.asthenia0412.pipelinestatemachine.strategy.ExecutionStrategy;
import io.github.asthenia0412.pipelinestatemachine.strategy.FullExecutionStrategy;
import io.github.asthenia0412.pipelinestatemachine.strategy.IncrementalExecutionStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Schedules;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TestPipelineService {
    private static final Logger logger = LoggerFactory.getLogger(TestPipelineService.class);
    private final TestTaskMapper taskMapper;
    private final StateMachineEngine stateMachine;
    private final Map<String, ExecutionStrategy> strategies;

    @Autowired
    public TestPipelineService(TestTaskMapper taskMapper, StateMachineEngine stateMachine) {
        this.taskMapper = taskMapper;
        this.stateMachine = stateMachine;
        this.strategies = new ConcurrentHashMap<>();
        strategies.put("FULL", new FullExecutionStrategy());
        strategies.put("INCREMENTAL", new IncrementalExecutionStrategy());
        strategies.put("CUSTOM", new CustomExecutionStrategy("custom-id"));
    }
    public void submitTask(TestTask task) {
        logger.info("Submitting task: {}", task.getTaskId());
        taskMapper.insertTask(task);
    }



    @Scheduled(fixedRate = 1000)
    public void pollTask() {
        logger.info("从数据库中取出任务-定时任务，当前时间是"+ LocalDateTime.now());
        
        try {
            // 添加调试信息
            logger.info("开始调用findByStatus方法");
            List<TestTask> tasks = taskMapper.findByStatus(null);
            logger.info("成功获取到{}个任务", tasks.size());
            
            for(TestTask task : tasks) {
                // 无论Phase是什么，只要任务Status都是Pending 那都开始流转状态 任务Status为Pending意味着用户刚刚上传这个任务
                if (task.getTaskStatus() == TaskStatus.PENDING) {
                    stateMachine.transition(task, ExecutionPhase.COMPILE);
                    taskMapper.updateTask(task);
                } else if(task.getTaskStatus() == TaskStatus.RUNNING) {
                    // 任务Status为Running，就代表这个Task已经被上面的If启动了 现在需要开始Phase之间的流转
                    // 我们只允许当前Phase的Status为SUCEESS的Task继续往后流转
                    // 如果当前Phase不成功就留在原地
                    if (task.getPhaseStatus() == PhaseStatus.SUCCESS) {
                        ExecutionPhase nextPhase = getNextPhase(task.getCurrentPhase());
                        if (nextPhase != null) {
                            stateMachine.transition(task,nextPhase);
                            if (nextPhase == ExecutionPhase.EXECUTE) {
                                strategies.get(task.getExecutionMode()).execute(task);
                            }
                        }
                    }
                }
                if (task.getTaskStatus() == TaskStatus.COMPLETED || task.getTaskStatus() == TaskStatus.FAILED) {
                    taskMapper.deleteByTaskId(task.getTaskId());
                }
            }
        } catch (Exception e) {
            logger.error("调用findByStatus方法时发生错误", e);
            throw e;
        }
    }

    private ExecutionPhase getNextPhase(ExecutionPhase current) {
        switch (current) {
            case COMPILE:
                return ExecutionPhase.EXECUTE;
            case EXECUTE:
                return ExecutionPhase.VALIDATE;
            default:
                return null;
        }
    }

}
