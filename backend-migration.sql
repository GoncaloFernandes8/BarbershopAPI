-- Migration: Create notifications table
-- Version: V7__create_notifications_table.sql
-- IMPORTANTE: Execute este SQL diretamente no banco de dados PostgreSQL (Neon.tech)
-- pois o Flyway está desabilitado em produção

CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(20) NOT NULL CHECK (type IN ('APPOINTMENT', 'CLIENT', 'SERVICE', 'SYSTEM')),
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    icon VARCHAR(50) NOT NULL,
    action_url VARCHAR(500),
    read_status BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Create indexes for better performance
CREATE INDEX idx_notifications_type ON notifications (type);
CREATE INDEX idx_notifications_read_status ON notifications (read_status);
CREATE INDEX idx_notifications_created_at ON notifications (created_at);
CREATE INDEX idx_notifications_unread ON notifications (read_status, created_at) WHERE read_status = FALSE;

-- Create function to automatically update updated_at
CREATE OR REPLACE FUNCTION update_notifications_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create trigger to automatically update updated_at
CREATE TRIGGER update_notifications_updated_at
    BEFORE UPDATE ON notifications
    FOR EACH ROW
    EXECUTE FUNCTION update_notifications_updated_at();

-- ============================================
-- V8: Create set_password_token table
-- ============================================

-- Create set_password_token table
CREATE TABLE set_password_token (
    id BIGSERIAL PRIMARY KEY,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    client_id BIGINT NOT NULL REFERENCES client(id) ON DELETE CASCADE,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    used_at TIMESTAMPTZ
);

-- Create indexes for better performance
CREATE INDEX idx_set_password_token_hash ON set_password_token (token_hash);
CREATE INDEX idx_set_password_token_client ON set_password_token (client_id);
CREATE INDEX idx_set_password_token_expires ON set_password_token (expires_at);
