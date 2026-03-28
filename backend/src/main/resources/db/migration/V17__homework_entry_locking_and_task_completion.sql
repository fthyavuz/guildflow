-- Add locking to task_progress entries
-- locked: true once the student saves the day (cannot re-edit)
-- done_permanently: true for CHECKBOX tasks once marked done (only mentor can reverse)
ALTER TABLE task_progress
    ADD COLUMN locked            BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN done_permanently  BOOLEAN NOT NULL DEFAULT FALSE;

-- Task completion approvals
-- Mentor/admin approves a specific task for a specific student on a specific assignment
CREATE TABLE task_completion (
    id            BIGSERIAL PRIMARY KEY,
    assignment_id BIGINT    NOT NULL REFERENCES class_homework_assignments(id) ON DELETE CASCADE,
    task_id       BIGINT    NOT NULL REFERENCES goal_tasks(id)                 ON DELETE CASCADE,
    student_id    BIGINT    NOT NULL REFERENCES users(id),
    approved_by   BIGINT    NOT NULL REFERENCES users(id),
    approved_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    notes         TEXT,
    CONSTRAINT uq_task_completion UNIQUE (assignment_id, task_id, student_id)
);

CREATE INDEX idx_task_completion_student    ON task_completion(student_id);
CREATE INDEX idx_task_completion_assignment ON task_completion(assignment_id);
