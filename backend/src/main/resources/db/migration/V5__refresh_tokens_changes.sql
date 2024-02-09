ALTER TABLE refresh_tokens DROP COLUMN access_token_hash;
ALTER TABLE refresh_tokens ADD fingerprint VARCHAR NOT NULL DEFAULT '';
