ALTER TABLE goals ALTER COLUMN class_id DROP NOT NULL;
ALTER TABLE goals ADD COLUMN created_by BIGINT REFERENCES users(id);
ALTER TABLE goals ADD COLUMN is_template BOOLEAN NOT NULL DEFAULT FALSE;

-- Update existing goals to have a created_by (set to their class mentor for now)
UPDATE goals g SET created_by = (SELECT mentor_id FROM mentor_classes mc WHERE mc.id = g.class_id);

-- Make created_by NOT NULL after initial update
ALTER TABLE goals ALTER COLUMN created_by SET NOT NULL;
