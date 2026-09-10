CREATE TABLE IF NOT EXISTS goals (
    id              TEXT PRIMARY KEY,
    name            TEXT NOT NULL,
    target_amount   NUMERIC(19, 2) NOT NULL,
    current_amount  NUMERIC(19, 2) NOT NULL,
    status          TEXT NOT NULL,
    version         INTEGER NOT NULL DEFAULT 0,
    CHECK (target_amount > 0),
    CHECK (current_amount >= 0),
    CHECK (current_amount <= target_amount),
    CHECK (status IN ('OPEN', 'COMPLETED'))
);
