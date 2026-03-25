-- =============================================
-- V13: Evolve Resource Library
-- Replaces hardcoded SourceType enum with a
-- dynamic resource_categories table.
-- Adds totalCapacity, dailyLimit, trackingType.
-- =============================================

-- 1. Dynamic category table (admin-configurable, no code changes needed)
CREATE TABLE resource_categories (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. Seed default categories (migrate from old SourceType enum values)
INSERT INTO resource_categories (name, description) VALUES
    ('Book',                 'Physical or digital books — tracked by page count'),
    ('Podcast',              'Audio content — tracked by duration in minutes'),
    ('Video',                'Video content — tracked by duration in minutes'),
    ('Quran Memorization',   'Surah or verse memorization — binary milestone (0% → 100% on mentor approval)');

-- 3. Extend sources table
ALTER TABLE sources
    ADD COLUMN category_id    BIGINT REFERENCES resource_categories(id),
    ADD COLUMN total_capacity DOUBLE PRECISION,
    ADD COLUMN daily_limit    DOUBLE PRECISION,
    ADD COLUMN tracking_type  VARCHAR(20) CHECK (tracking_type IN ('LINEAR', 'BINARY'));

-- 4. Migrate existing type data → category_id
UPDATE sources SET category_id = (SELECT id FROM resource_categories WHERE name = 'Book')    WHERE type = 'BOOK';
UPDATE sources SET category_id = (SELECT id FROM resource_categories WHERE name = 'Podcast') WHERE type = 'PODCAST';
UPDATE sources SET category_id = (SELECT id FROM resource_categories WHERE name = 'Video')   WHERE type = 'VIDEO';

-- 5. Migrate total_pages / total_minutes → total_capacity
UPDATE sources SET total_capacity = total_pages   WHERE total_pages IS NOT NULL;
UPDATE sources SET total_capacity = total_minutes WHERE total_minutes IS NOT NULL AND total_pages IS NULL;

-- 6. Default tracking type for migrated records
UPDATE sources SET tracking_type = 'LINEAR' WHERE category_id IS NOT NULL AND tracking_type IS NULL;

-- 7. Make old type column nullable (kept for backward-compat, no longer used)
ALTER TABLE sources ALTER COLUMN type DROP NOT NULL;

-- 8. Indexes
CREATE INDEX idx_sources_category ON sources(category_id);
CREATE INDEX idx_resource_categories_active ON resource_categories(active);
