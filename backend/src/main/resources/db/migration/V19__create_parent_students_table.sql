CREATE TABLE parent_students (
    id         BIGSERIAL PRIMARY KEY,
    parent_id  BIGINT NOT NULL REFERENCES users(id),
    student_id BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE (parent_id, student_id)
);

CREATE INDEX idx_parent_students_parent  ON parent_students(parent_id);
CREATE INDEX idx_parent_students_student ON parent_students(student_id);
