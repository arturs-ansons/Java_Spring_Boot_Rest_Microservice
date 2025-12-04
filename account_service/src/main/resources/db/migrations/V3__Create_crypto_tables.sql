CREATE TABLE crypto_transactions (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL,

    -- Transaction Identification
    transaction_type VARCHAR(20) NOT NULL CHECK (transaction_type IN ('BUY', 'SELL', 'DEPOSIT', 'WITHDRAWAL', 'TRANSFER', 'SWAP', 'STAKE', 'UNSTAKE', 'REWARD')),
    crypto_currency VARCHAR(10) NOT NULL,
    crypto_amount NUMERIC(36,18) NOT NULL,

    -- Fiat Information
    fiat_currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    fiat_amount NUMERIC(19,2) NOT NULL,
    price_per_unit NUMERIC(36,18) NOT NULL,

    -- Wallet Addresses
    from_address VARCHAR(255),
    to_address VARCHAR(255),
    transaction_hash VARCHAR(255),

    -- Network Information
    network VARCHAR(50),
    network_fee NUMERIC(36,18),
    network_fee_fiat NUMERIC(19,2),

    -- Balance Tracking
    crypto_balance_before NUMERIC(36,18) NOT NULL,
    crypto_balance_after NUMERIC(36,18) NOT NULL,
    fiat_balance_before NUMERIC(19,2) NOT NULL,
    fiat_balance_after NUMERIC(19,2) NOT NULL,

    -- Status & Timing
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'CONFIRMING', 'COMPLETED', 'FAILED', 'CANCELLED', 'EXPIRED')),
    confirmation_count INTEGER DEFAULT 0,
    required_confirmations INTEGER DEFAULT 1,
    confirmed_at TIMESTAMP,

    -- Additional Fields
    description TEXT,
    reference VARCHAR(100),
    external_id VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Key
    CONSTRAINT fk_crypto_transactions_account FOREIGN KEY (account_id) REFERENCES accounts(id),

    -- Unique Constraints
    CONSTRAINT uq_transaction_hash UNIQUE (transaction_hash),

    -- Additional Constraints
    CONSTRAINT chk_crypto_amount_positive CHECK (crypto_amount > 0),
    CONSTRAINT chk_fiat_amount_positive CHECK (fiat_amount > 0),
    CONSTRAINT chk_price_positive CHECK (price_per_unit > 0)
);

-- Indexes for Performance
CREATE INDEX idx_crypto_transactions_account_id ON crypto_transactions(account_id);
CREATE INDEX idx_crypto_transactions_created_at ON crypto_transactions(created_at);
CREATE INDEX idx_crypto_transactions_type ON crypto_transactions(transaction_type);
CREATE INDEX idx_crypto_transactions_currency ON crypto_transactions(crypto_currency);
CREATE INDEX idx_crypto_transactions_status ON crypto_transactions(status);
CREATE INDEX idx_crypto_transactions_hash ON crypto_transactions(transaction_hash);
CREATE INDEX idx_crypto_transactions_external_id ON crypto_transactions(external_id);
CREATE INDEX idx_crypto_transactions_account_currency ON crypto_transactions(account_id, crypto_currency);
CREATE INDEX idx_crypto_transactions_account_created_desc ON crypto_transactions(account_id, created_at DESC);
CREATE INDEX idx_crypto_transactions_pending ON crypto_transactions(status) WHERE status IN ('PENDING', 'CONFIRMING');
CREATE INDEX idx_crypto_transactions_confirmed_at ON crypto_transactions(confirmed_at) WHERE confirmed_at IS NOT NULL;

-- Crypto Accounts Table
CREATE TABLE crypto_accounts (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL,
    crypto_currency VARCHAR(10) NOT NULL,
    wallet_address VARCHAR(255),
    balance NUMERIC(36,18) NOT NULL DEFAULT 0,
    available_balance NUMERIC(36,18) NOT NULL DEFAULT 0,
    locked_balance NUMERIC(36,18) NOT NULL DEFAULT 0,
    average_buy_price NUMERIC(36,18),
    total_invested NUMERIC(19,2) DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_crypto_accounts_account FOREIGN KEY (account_id) REFERENCES accounts(id),
    CONSTRAINT unique_account_currency UNIQUE (account_id, crypto_currency),
    CONSTRAINT chk_crypto_balance_non_negative CHECK (balance >= 0 AND available_balance >= 0 AND locked_balance >= 0)
);

CREATE INDEX idx_crypto_accounts_account ON crypto_accounts(account_id);
CREATE INDEX idx_crypto_accounts_wallet ON crypto_accounts(wallet_address);
CREATE INDEX idx_crypto_accounts_currency ON crypto_accounts(crypto_currency);
CREATE INDEX idx_crypto_accounts_balance ON crypto_accounts(balance) WHERE balance > 0;
CREATE INDEX idx_crypto_accounts_updated ON crypto_accounts(updated_at);
