-- Database initialization script for Pipeline State Machine
-- This script creates the necessary tables for the application

-- Create test_task table
CREATE TABLE IF NOT EXISTS test_task (
    task_id VARCHAR(100) PRIMARY KEY,
    test_case_id VARCHAR(100) NOT NULL,
    execution_mode VARCHAR(50) NOT NULL,
    task_status VARCHAR(50) NOT NULL,
    current_phase VARCHAR(50) NOT NULL,
    phase_status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_test_task_status ON test_task(task_status);
CREATE INDEX IF NOT EXISTS idx_test_task_created_at ON test_task(created_at);
CREATE INDEX IF NOT EXISTS idx_test_task_test_case_id ON test_task(test_case_id);
