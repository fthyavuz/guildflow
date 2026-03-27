-- goal_type_id is now optional (category removed from homework templates)
ALTER TABLE goals ALTER COLUMN goal_type_id DROP NOT NULL;
