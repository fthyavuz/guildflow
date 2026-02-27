-- =============================================
-- V5: Create student evaluations table
-- GuildFlow Phase 5 - Student Evaluations
-- =============================================

CREATE TABLE student_evaluations (
    id              BIGSERIAL PRIMARY KEY,
    mentor_id       BIGINT NOT NULL REFERENCES users(id),
    student_id      BIGINT NOT NULL REFERENCES users(id),
    period          VARCHAR(20) NOT NULL CHECK (period IN ('WEEKLY', 'MONTHLY', 'QUARTERLY', 'CUSTOM')),
    content         TEXT NOT NULL,
    period_name     VARCHAR(255),
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_evaluations_mentor ON student_evaluations(mentor_id);
CREATE INDEX idx_evaluations_student ON student_evaluations(student_id);
