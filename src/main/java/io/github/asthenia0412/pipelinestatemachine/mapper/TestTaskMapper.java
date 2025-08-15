package io.github.asthenia0412.pipelinestatemachine.mapper;

import io.github.asthenia0412.pipelinestatemachine.model.task.TestTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TestTaskMapper {

    void insertTask(TestTask task);

    void updateTask(TestTask task);

    TestTask findByTaskId(String taskId);

    List<TestTask> findByStatus(@Param("status")String status);

    void deleteByTaskId(String taskId);

    void batchInsertTasks(List<TestTask> tasks);
}
