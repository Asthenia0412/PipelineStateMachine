package io.github.asthenia0412.pipelinestatemachine.service;

import io.github.asthenia0412.pipelinestatemachine.mapper.TestTaskMapper;
import io.github.asthenia0412.pipelinestatemachine.model.enums.ExecutionPhase;
import io.github.asthenia0412.pipelinestatemachine.model.enums.PhaseStatus;
import io.github.asthenia0412.pipelinestatemachine.model.enums.TaskStatus;
import io.github.asthenia0412.pipelinestatemachine.model.task.TestTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 任务状态机引擎
 * <p>
 * 负责处理测试任务的状态转换和阶段流转逻辑，核心功能包括：
 * 1. 管理任务的生命周期状态（PENDING/RUNNING/COMPLETED/FAILED）
 * 2. 控制执行阶段的顺序流转（COMPILE → EXECUTE → VALIDATE）
 * 3. 维护每个阶段的处理状态（IN_PROCESSING/SUCCESS/ERROR）
 * </p>
 *
 * @author asthenia0412
 * @since 2023-08-15
 */
@Component
public class StateMachineEngine {
    private static final Logger logger = LoggerFactory.getLogger(StateMachineEngine.class);

    @Autowired
    private TestTaskMapper testTaskMapper;

    /**
     * 执行任务状态转换
     * <p>
     * 根据目标阶段更新任务状态，处理逻辑：
     * 1. COMPILE阶段：自动标记为成功（模拟编译通过）
     * 2. EXECUTE阶段：等待外部策略执行
     * 3. VALIDATE阶段：标记任务最终完成
     * 4. 无效阶段：标记任务失败
     * </p>
     *
     * @param task      当前处理的任务对象
     * @param nextPhase 目标阶段枚举值
     * @throws IllegalArgumentException 如果任务对象为空
     */
    public void transition(TestTask task, ExecutionPhase nextPhase) {
        if (task == null) {
            throw new IllegalArgumentException("任务对象不能为null");
        }

        logger.info("任务阶段转换 | 任务ID:{} | 当前阶段:{} → 目标阶段:{}",
                task.getTaskId(), task.getCurrentPhase(), nextPhase);

        switch (nextPhase) {
            case COMPILE:
                handleCompilePhase(task);
                break;
            case EXECUTE:
                handleExecutePhase(task);
                break;
            case VALIDATE:
                handleValidatePhase(task);
                break;
            default:
                handleInvalidPhase(task);
        }
    }

    /**
     * 处理编译阶段状态转换
     * <p>
     * 特殊逻辑：自动将阶段状态标记为SUCCESS（模拟编译成功）
     * </p>
     *
     * @param task 当前任务对象
     */
    private void handleCompilePhase(TestTask task) {
        task.setTaskStatus(TaskStatus.RUNNING);
        task.setCurrentPhase(ExecutionPhase.COMPILE);
        task.setPhaseStatus(PhaseStatus.IN_PROCESSING);
        // 模拟编译立即成功
        task.setPhaseStatus(PhaseStatus.SUCCESS);
        testTaskMapper.updateTask(task);
        logger.debug("编译阶段处理完成 | 任务ID:{}", task.getTaskId());
    }

    /**
     * 处理执行阶段状态转换
     * <p>
     * 仅更新状态，实际执行由外部策略处理
     * </p>
     *
     * @param task 当前任务对象
     */
    private void handleExecutePhase(TestTask task) {
        task.setTaskStatus(TaskStatus.RUNNING);
        task.setCurrentPhase(ExecutionPhase.EXECUTE);
        task.setPhaseStatus(PhaseStatus.IN_PROCESSING);
        testTaskMapper.updateTask(task);
        logger.debug("执行阶段已准备 | 任务ID:{}", task.getTaskId());
    }

    /**
     * 处理验证阶段状态转换
     * <p>
     * 标记任务最终完成状态
     * </p>
     *
     * @param task 当前任务对象
     */
    private void handleValidatePhase(TestTask task) {
        task.setTaskStatus(TaskStatus.RUNNING);
        task.setCurrentPhase(ExecutionPhase.VALIDATE);
        // 模拟验证立即成功
        task.setPhaseStatus(PhaseStatus.SUCCESS);
        task.setTaskStatus(TaskStatus.COMPLETED);
        testTaskMapper.updateTask(task);
        logger.info("任务验证完成 | 任务ID:{}", task.getTaskId());
    }

    /**
     * 处理无效阶段状态转换
     * <p>
     * 将任务标记为失败状态
     * </p>
     *
     * @param task 当前任务对象
     */
    private void handleInvalidPhase(TestTask task) {
        logger.error("无效的阶段转换 | 任务ID:{}", task.getTaskId());
        task.setTaskStatus(TaskStatus.FAILED);
        task.setPhaseStatus(PhaseStatus.ERROR);
        testTaskMapper.updateTask(task);
    }
}