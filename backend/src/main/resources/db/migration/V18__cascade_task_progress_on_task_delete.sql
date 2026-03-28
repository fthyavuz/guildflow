-- Drop existing FK constraints and re-add with ON DELETE CASCADE
-- so that deleting a GoalTask automatically removes its progress and completion records

ALTER TABLE task_progress
    DROP CONSTRAINT IF EXISTS task_progress_task_id_fkey,
    DROP CONSTRAINT IF EXISTS fk_task_progress_task;

ALTER TABLE task_progress
    ADD CONSTRAINT task_progress_task_id_fkey
        FOREIGN KEY (task_id) REFERENCES goal_tasks(id) ON DELETE CASCADE;

ALTER TABLE task_completion
    DROP CONSTRAINT IF EXISTS task_completion_task_id_fkey,
    DROP CONSTRAINT IF EXISTS fk_task_completion_task;

ALTER TABLE task_completion
    ADD CONSTRAINT task_completion_task_id_fkey
        FOREIGN KEY (task_id) REFERENCES goal_tasks(id) ON DELETE CASCADE;
