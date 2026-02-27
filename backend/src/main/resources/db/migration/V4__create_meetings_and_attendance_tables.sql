-- =============================================
-- V4: Create meetings and attendance tables
-- GuildFlow Phase 4 - Meetings & Attendance
-- =============================================

CREATE TABLE meetings (
    id                  BIGSERIAL PRIMARY KEY,
    class_id            BIGINT NOT NULL REFERENCES mentor_classes(id),
    title               VARCHAR(255) NOT NULL,
    description         TEXT,
    start_time          TIMESTAMP NOT NULL,
    end_time            TIMESTAMP NOT NULL,
    location            VARCHAR(255),
    is_recurring        BOOLEAN NOT NULL DEFAULT FALSE,
    recurrence_group_id VARCHAR(255),
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE attendance (
    id              BIGSERIAL PRIMARY KEY,
    meeting_id      BIGINT NOT NULL REFERENCES meetings(id),
    student_id      BIGINT NOT NULL REFERENCES users(id),
    status          VARCHAR(20) NOT NULL CHECK (status IN ('PRESENT', 'ABSENT', 'EXCUSED', 'LATE')),
    note            TEXT,
    recorded_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(meeting_id, student_id)
);

-- Indexes for performance
CREATE INDEX idx_meetings_class ON meetings(class_id);
CREATE INDEX idx_meetings_start ON meetings(start_time);
CREATE INDEX idx_attendance_meeting ON attendance(meeting_id);
CREATE INDEX idx_attendance_student ON attendance(student_id);
