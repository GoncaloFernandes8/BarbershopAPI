-- Coluna no cliente/utilizador (ajusta o nome da tabela/coluna conforme o teu modelo)
ALTER TABLE client
  ADD COLUMN IF NOT EXISTS email_verified_at timestamptz;

-- Tabela de tokens de verificação
CREATE TABLE IF NOT EXISTS email_verification_token (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id bigint NOT NULL REFERENCES client(id) ON DELETE CASCADE, -- ajusta o tipo/PK se usares UUID
  token_hash text NOT NULL UNIQUE,
  expires_at timestamptz NOT NULL,
  used_at timestamptz,
  created_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_email_verif_user ON email_verification_token(user_id);
