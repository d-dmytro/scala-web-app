CREATE TABLE refresh_tokens (
  id UUID NOT NULL,
  user_id UUID NOT NULL,
  token_hash VARCHAR NOT NULL,
  created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL
);
