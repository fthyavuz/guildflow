-- =============================================
-- V14: Homework approval workflow + goal frequency
-- =============================================

-- 1. Add frequency to goals (DAILY / WEEKLY, nullable — templates have no frequency)
ALTER TABLE goals
    ADD COLUMN frequency VARCHAR(10) CHECK (frequency IN ('DAILY', 'WEEKLY'));

-- 2. Add approval workflow columns to task_progress
ALTER TABLE task_progress
    ADD COLUMN status        VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                             CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    ADD COLUMN mentor_notes  TEXT,
    ADD COLUMN reviewed_by   BIGINT REFERENCES users(id),
    ADD COLUMN reviewed_at   TIMESTAMP;

-- 3. Backfill: existing entries are considered approved
UPDATE task_progress SET status = 'APPROVED' WHERE status = 'PENDING';

-- 4. Indexes for approval queue queries
CREATE INDEX idx_task_progress_status      ON task_progress(status);
CREATE INDEX idx_task_progress_reviewed_by ON task_progress(reviewed_by);
