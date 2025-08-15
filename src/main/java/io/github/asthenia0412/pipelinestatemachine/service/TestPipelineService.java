package io.github.asthenia0412.pipelinestatemachine.service;

import io.github.asthenia0412.pipelinestatemachine.mapper.TestTaskMapper;
import io.github.asthenia0412.pipelinestatemachine.model.enums.ExecutionPhase;
import io.github.asthenia0412.pipelinestatemachine.model.enums.PhaseStatus;
import io.github.asthenia0412.pipelinestatemachine.model.enums.TaskStatus;
import io.github.asthenia0412.pipelinestatemachine.model.task.TestTask;
import io.github.asthenia0412.pipelinestatemachine.strategy.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 测试流水线任务处理服务
 * <p>
 * 核心功能：
 * 1. 接收并提交测试任务
 * 2. 定时轮询处理任务状态流转
 * 3. 根据执行模式选择不同策略执行任务
 * </p>
 *
 * @author asthenia0412
 * @since 2023-08-15
 */
@Service
public class TestPipelineService {
    private static final Logger logger = LoggerFactory.getLogger(TestPipelineService.class);
    private final TestTaskMapper taskMapper;
    private final StateMachineEngine stateMachine;
    private final Map<String, ExecutionStrategy> strategies;

    /**
     * 构造函数初始化服务
     *
     * @param taskMapper   任务数据库访问接口
     * @param stateMachine 状态机引擎
     */
    @Autowired
    public TestPipelineService(TestTaskMapper taskMapper, StateMachineEngine stateMachine) {
        this.taskMapper = taskMapper;
        this.stateMachine = stateMachine;
        this.strategies = new ConcurrentHashMap<>();
        // 初始化执行策略映射
        strategies.put("FULL", new FullExecutionStrategy());
        strategies.put("INCREMENTAL", new IncrementalExecutionStrategy());
        strategies.put("CUSTOM", new CustomExecutionStrategy("custom-id"));
    }

    /**
     * 提交测试任务到系统
     * <p>
     * 将任务持久化到数据库，等待后续处理
     * </p>
     *
     * @param task 测试任务对象，包含任务ID、用例ID、执行模式等基本信息
     * @throws RuntimeException 如果数据库操作失败
     */
    public void submitTask(TestTask task) {
        logger.info("正在提交任务: {}", task.getTaskId());
        try {
            taskMapper.insertTask(task);
        } catch (Exception e) {
            logger.error("提交任务失败", e);
            throw new RuntimeException("任务提交失败", e);
        }
    }

    /**
     * 定时轮询任务处理
     * <p>
     * 每秒钟执行一次，主要功能：
     * 1. 获取待处理任务列表
     * 2. 处理不同状态的任务流转
     * 3. 清理已完成任务
     * </p>
     *
     * @see Scheduled 定时任务注解，固定频率1000ms
     */
    @Scheduled(fixedRate = 1000)
    public void pollTask() {
        logger.info("任务轮询开始 | 当前时间: {}", LocalDateTime.now());

        try {
            List<TestTask> tasks = taskMapper.findByStatus(null);
            logger.debug("获取到待处理任务数量: {}", tasks.size());

            processTasks(tasks);
        } catch (Exception e) {
            logger.error("任务轮询处理异常", e);
            throw new RuntimeException("任务处理流程中断", e);
        }
    }

    /**
     * 处理任务状态流转
     *
     * @param tasks 待处理任务列表
     */
    private void processTasks(List<TestTask> tasks) {
        for (TestTask task : tasks) {
            try {
                handleTaskStatus(task);
                cleanupCompletedTask(task);
            } catch (Exception e) {
                logger.error("处理任务异常 | 任务ID: {}", task.getTaskId(), e);
            }
        }
    }

    /**
     * 处理任务状态转换
     *
     * @param task 当前处理的任务对象
     */
    private void handleTaskStatus(TestTask task) {
        // 新任务开始流转
        if (task.getTaskStatus() == TaskStatus.PENDING) {
            startTaskProcessing(task);
        }
        // 运行中任务阶段流转
        else if (task.getTaskStatus() == TaskStatus.RUNNING) {
            processRunningTask(task);
        }
    }

    /**
     * 启动任务处理流程
     *
     * @param task 待启动的任务
     */
    private void startTaskProcessing(TestTask task) {
        stateMachine.transition(task, ExecutionPhase.COMPILE);
        taskMapper.updateTask(task);
        logger.info("任务已启动 | 任务ID: {}", task.getTaskId());
    }

    /**
     * 处理运行中任务的状态流转
     *
     * @param task 运行中的任务
     */
    private void processRunningTask(TestTask task) {
        if (task.getPhaseStatus() == PhaseStatus.SUCCESS) {
            ExecutionPhase nextPhase = getNextPhase(task.getCurrentPhase());
            if (nextPhase != null) {
                stateMachine.transition(task, nextPhase);
                executePhaseStrategy(task, nextPhase);
            }
        }
    }

    /**
     * 执行阶段对应的策略
     *
     * @param task      当前任务
     * @param nextPhase 下一个执行阶段
     */
    private void executePhaseStrategy(TestTask task, ExecutionPhase nextPhase) {
        if (nextPhase == ExecutionPhase.EXECUTE) {
            strategies.get(task.getExecutionMode()).execute(task);
            logger.debug("执行策略完成 | 任务ID: {} | 模式: {}", task.getTaskId(), task.getExecutionMode());
        }
    }

    /**
     * 清理已完成任务
     *
     * @param task 待检查的任务
     */
    private void cleanupCompletedTask(TestTask task) {
        if (task.getTaskStatus() == TaskStatus.COMPLETED || task.getTaskStatus() == TaskStatus.FAILED) {
            taskMapper.deleteByTaskId(task.getTaskId());
            logger.info("任务已清理 | 任务ID: {} | 状态: {}", task.getTaskId(), task.getTaskStatus());
        }
    }

    /**
     * 获取下一处理阶段
     * <p>
     * 当前阶段流转规则：
     * COMPILE → EXECUTE → VALIDATE → null
     * </p>
     *
     * @param current 当前阶段
     * @return 下一阶段，如果已是最后阶段返回null
     */
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