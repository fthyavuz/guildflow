-- =============================================
-- V2: Create classes tables
-- GuildFlow Phase 2 - Class Management
-- =============================================

CREATE TABLE mentor_classes (
    id              BIGSERIAL PRIMARY KEY,
    mentor_id       BIGINT NOT NULL REFERENCES users(id),
    name            VARCHAR(255) NOT NULL,
    education_level VARCHAR(20) NOT NULL CHECK (education_level IN ('PRIMARY', 'SECONDARY', 'HIGH_SCHOOL')),
    description     TEXT,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE class_students (
    id              BIGSERIAL PRIMARY KEY,
    class_id        BIGINT NOT NULL REFERENCES mentor_classes(id),
    student_id      BIGINT NOT NULL REFERENCES users(id),
    enrolled_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    left_at         TIMESTAMP,
    UNIQUE(class_id, student_id)
);

-- Index for class lookups by mentor
CREATE INDEX idx_mentor_classes_mentor ON mentor_classes(mentor_id);

-- Index for searching active student class enrollments
CREATE INDEX idx_class_students_student_active ON class_students(student_id) WHERE active = TRUE;
