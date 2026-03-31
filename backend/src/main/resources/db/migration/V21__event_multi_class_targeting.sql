-- Replace single target_class_id with a multi-class join table
-- Events can now invite multiple classes; visibility is class-based for students/parents

CREATE TABLE event_target_classes (
    event_id BIGINT NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    class_id BIGINT NOT NULL REFERENCES mentor_classes(id) ON DELETE CASCADE,
    PRIMARY KEY (event_id, class_id)
);

-- Migrate existing single-class targets
INSERT INTO event_target_classes (event_id, class_id)
SELECT id, target_class_id FROM events WHERE target_class_id IS NOT NULL;

-- Drop the old single-class columns
DROP INDEX IF EXISTS idx_events_target_class_id;
ALTER TABLE events DROP COLUMN target_class_id;

CREATE INDEX idx_event_target_classes_event ON event_target_classes(event_id);
CREATE INDEX idx_event_target_classes_class  ON event_target_classes(class_id);
