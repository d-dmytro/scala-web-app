CREATE TABLE invoices (
  id UUID NOT NULL,
  amount BIGINT DEFAULT 0 NOT NULL,
  created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL
);
