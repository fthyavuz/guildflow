CREATE TABLE sources (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    language VARCHAR(50),
    part VARCHAR(255),
    total_pages INTEGER,
    total_minutes INTEGER,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

ALTER TABLE goal_tasks ADD COLUMN source_id BIGINT REFERENCES sources(id);
