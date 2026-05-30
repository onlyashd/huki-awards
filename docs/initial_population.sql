-- Huki Awards - Full Database Initialization Script
-- This script creates all tables, indexes, and initial data for a fresh installation.

-- 1. Enable UUID support
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- 2. Create Tables

-- Users Table
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username TEXT,
    name TEXT,
    avatar_url TEXT,
    provider TEXT DEFAULT 'DISCORD',
    role TEXT DEFAULT 'USER'
);

-- Admins Table (Access Control)
CREATE TABLE IF NOT EXISTS admins (
    id SERIAL PRIMARY KEY,
    username TEXT UNIQUE NOT NULL
);

-- Settings Table
CREATE TABLE IF NOT EXISTS settings (
    id SERIAL PRIMARY KEY,
    event_name TEXT DEFAULT 'Game Awards',
    voting_start TEXT,
    voting_end TEXT,
    is_voting_open BOOLEAN DEFAULT TRUE,
    show_dates_to_users BOOLEAN DEFAULT TRUE,
    phase TEXT DEFAULT 'NOMINATION'
);

-- Categories Table
CREATE TABLE IF NOT EXISTS categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT,
    description TEXT,
    weight INTEGER DEFAULT 0
);

-- Votes Table
CREATE TABLE IF NOT EXISTS votes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id UUID NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    igdb_game_id BIGINT NOT NULL,
    game_name TEXT,
    game_cover_url TEXT
);

-- Audit Logs Table
CREATE TABLE IF NOT EXISTS audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    admin_username TEXT NOT NULL,
    action TEXT NOT NULL,
    target TEXT,
    details TEXT
);

-- 3. Create Indexes for Performance
CREATE INDEX IF NOT EXISTS idx_votes_user_category ON votes(user_id, category_id);
CREATE INDEX IF NOT EXISTS idx_votes_category ON votes(category_id);
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);

-- 4. Initial Population

-- Default Settings
INSERT INTO settings (event_name, is_voting_open, show_dates_to_users, phase)
VALUES ('Game Awards', true, true, 'NOMINATION')
ON CONFLICT DO NOTHING;

-- Initial Administrators
-- REPLACE 'your_discord_username' with your actual Discord username to access the dashboard.
INSERT INTO admins (username) VALUES ('your_discord_username')
ON CONFLICT (username) DO NOTHING;

-- Sample Categories
INSERT INTO categories (id, name, description, weight) VALUES
(gen_random_uuid(), 'Game of the Year', 'The best overall gaming experience of the year.', 1),
(gen_random_uuid(), 'Best Art Direction', 'For outstanding creative and technical achievement in artistic design.', 2),
(gen_random_uuid(), 'Best Narrative', 'For outstanding storytelling and narrative development.', 3),
(gen_random_uuid(), 'Most Anticipated', 'The game you are most looking forward to playing.', 4)
ON CONFLICT DO NOTHING;
