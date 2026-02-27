-- =============================================
-- V3: Create goals and tasks tables
-- GuildFlow Phase 3 - Goal & Task Management
-- =============================================

CREATE TABLE goal_types (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) UNIQUE NOT NULL,
    description     TEXT,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE goals (
    id              BIGSERIAL PRIMARY KEY,
    title           VARCHAR(255) NOT NULL,
    description     TEXT,
    class_id        BIGINT NOT NULL REFERENCES mentor_classes(id),
    goal_type_id    BIGINT NOT NULL REFERENCES goal_types(id),
    apply_to_all    BOOLEAN NOT NULL DEFAULT TRUE,
    start_date      TIMESTAMP,
    end_date        TIMESTAMP,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE goal_tasks (
    id              BIGSERIAL PRIMARY KEY,
    goal_id         BIGINT NOT NULL REFERENCES goals(id),
    title           VARCHAR(255) NOT NULL,
    description     TEXT,
    task_type       VARCHAR(20) NOT NULL CHECK (task_type IN ('CHECKBOX', 'NUMBER')),
    target_value    DOUBLE PRECISION,
    sort_order      INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE goal_students (
    id              BIGSERIAL PRIMARY KEY,
    goal_id         BIGINT NOT NULL REFERENCES goals(id),
    student_id      BIGINT NOT NULL REFERENCES users(id),
    UNIQUE(goal_id, student_id)
);

CREATE TABLE task_progress (
    id              BIGSERIAL PRIMARY KEY,
    task_id         BIGINT NOT NULL REFERENCES goal_tasks(id),
    student_id      BIGINT NOT NULL REFERENCES users(id),
    entry_date      DATE NOT NULL DEFAULT CURRENT_DATE,
    numeric_value   DOUBLE PRECISION,
    boolean_value   BOOLEAN,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE goal_student_reviews (
    id              BIGSERIAL PRIMARY KEY,
    goal_id         BIGINT NOT NULL REFERENCES goals(id),
    student_id      BIGINT NOT NULL REFERENCES users(id),
    completed       BOOLEAN NOT NULL DEFAULT FALSE,
    comment         TEXT,
    review_date     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(goal_id, student_id)
);

-- Indexes for performance
CREATE INDEX idx_goals_class ON goals(class_id);
CREATE INDEX idx_goal_tasks_goal ON goal_tasks(goal_id);
CREATE INDEX idx_task_progress_student ON task_progress(student_id);
CREATE INDEX idx_task_progress_task ON task_progress(task_id);
CREATE INDEX idx_task_progress_date ON task_progress(entry_date);
CREATE INDEX idx_goal_reviews_goal ON goal_student_reviews(goal_id);
CREATE INDEX idx_goal_reviews_student ON goal_student_reviews(student_id);
