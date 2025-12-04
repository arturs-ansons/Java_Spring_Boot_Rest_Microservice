CREATE TABLE accounts (
                          id BIGSERIAL PRIMARY KEY,
                          user_id BIGINT NOT NULL,
                          account_number VARCHAR(50) NOT NULL UNIQUE,
                          balance NUMERIC(19,2) NOT NULL DEFAULT 0.00,
                          status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- Changed to ACTIVE
                          type VARCHAR(20) NOT NULL,
                          crypto_enabled BOOLEAN NOT NULL DEFAULT FALSE, -- Added missing column
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP
);

CREATE INDEX idx_accounts_user_id ON accounts(user_id);
CREATE INDEX idx_accounts_account_number ON accounts(account_number);
CREATE INDEX idx_accounts_status ON accounts(status);
CREATE INDEX idx_accounts_crypto_enabled ON accounts(crypto_enabled) WHERE crypto_enabled = true;

ALTER TABLE accounts ADD CONSTRAINT chk_balance_non_negative CHECK (balance >= 0);
ALTER TABLE accounts ADD CONSTRAINT chk_status_valid
    CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'CLOSED', 'FROZEN'));
ALTER TABLE accounts ADD CONSTRAINT chk_type_valid
    CHECK (type IN ('CHECKING', 'SAVINGS', 'BUSINESS', 'LOAN', 'CREDIT', 'TRADING', 'CRYPTO'));