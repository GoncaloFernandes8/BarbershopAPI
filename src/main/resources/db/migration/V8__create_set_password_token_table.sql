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

