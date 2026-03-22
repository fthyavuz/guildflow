-- =============================================
-- V11: Add missing performance indexes
-- Fixes full table scans identified in TODO
-- =============================================

-- goal_tasks(goal_id) — already exists from V3 as idx_goal_tasks_goal, skipping duplicate

-- goal_students(student_id)
CREATE INDEX IF NOT EXISTS idx_goal_students_student ON goal_students(student_id);

-- class_students(student_id, active) — composite for active enrollment lookups
CREATE INDEX IF NOT EXISTS idx_class_students_student_active_composite ON class_students(student_id, active);

-- task_progress(task_id, student_id) — composite for progress lookups
CREATE INDEX IF NOT EXISTS idx_task_progress_task_student ON task_progress(task_id, student_id);
