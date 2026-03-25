-- Add audience targeting to events: optional education level and/or specific class
ALTER TABLE events
    ADD COLUMN education_level VARCHAR(20),
    ADD COLUMN target_class_id BIGINT REFERENCES mentor_classes(id) ON DELETE SET NULL;

CREATE INDEX idx_events_education_level ON events(education_level);
CREATE INDEX idx_events_target_class_id ON events(target_class_id);
