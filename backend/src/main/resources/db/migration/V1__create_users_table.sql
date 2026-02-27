-- =============================================
-- V1: Create users table
-- GuildFlow Phase 1 - Identity & Access
-- =============================================

CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    email           VARCHAR(255) UNIQUE NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    phone           VARCHAR(20),
    role            VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'MENTOR', 'STUDENT', 'PARENT')),
    language_pref   VARCHAR(5) NOT NULL DEFAULT 'TR' CHECK (language_pref IN ('TR', 'EN', 'DE')),
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index for email lookups (login)
CREATE INDEX idx_users_email ON users(email);

-- Index for role-based queries
CREATE INDEX idx_users_role ON users(role);

-- Index for active user queries
CREATE INDEX idx_users_active ON users(active);
