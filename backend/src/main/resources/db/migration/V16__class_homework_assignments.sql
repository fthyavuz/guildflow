-- Class Homework Assignment Engine
-- Stores template assignments to classes with scheduling config

CREATE TABLE class_homework_assignments (
    id          BIGSERIAL PRIMARY KEY,
    goal_id     BIGINT NOT NULL REFERENCES goals(id),
    class_id    BIGINT NOT NULL REFERENCES mentor_classes(id),
    frequency   VARCHAR(10),
    start_date  DATE,
    end_date    DATE,
    apply_to_all BOOLEAN NOT NULL DEFAULT TRUE,
    created_by  BIGINT REFERENCES users(id),
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Bridge table for targeted (non-apply-to-all) assignments
CREATE TABLE class_homework_assignment_students (
    assignment_id BIGINT NOT NULL REFERENCES class_homework_assignments(id) ON DELETE CASCADE,
    student_id    BIGINT NOT NULL REFERENCES users(id),
    PRIMARY KEY (assignment_id, student_id)
);

CREATE INDEX idx_cha_class_id ON class_homework_assignments(class_id);
CREATE INDEX idx_cha_goal_id  ON class_homework_assignments(goal_id);
