CREATE TABLE transactions (
                              id BIGSERIAL PRIMARY KEY,
                              account_id BIGINT NOT NULL,
                              type VARCHAR(20) NOT NULL,
                              amount NUMERIC(19,2) NOT NULL,
                              balance_before NUMERIC(19,2) NOT NULL,
                              balance_after NUMERIC(19,2) NOT NULL,
                              description TEXT,
                              reference VARCHAR(100),
                              status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                              CONSTRAINT fk_transactions_account FOREIGN KEY (account_id) REFERENCES accounts(id)
);

CREATE INDEX idx_transactions_account_id ON transactions(account_id);
CREATE INDEX idx_transactions_created_at ON transactions(created_at);
CREATE INDEX idx_transactions_type ON transactions(type);
CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_transactions_account_created ON transactions(account_id, created_at DESC);

-- Add constraints
ALTER TABLE transactions ADD CONSTRAINT chk_transaction_amount_non_zero CHECK (amount != 0);
ALTER TABLE transactions ADD CONSTRAINT chk_status_valid
    CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'CANCELLED'));