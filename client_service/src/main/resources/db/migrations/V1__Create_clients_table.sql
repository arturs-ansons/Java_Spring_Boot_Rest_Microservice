CREATE TABLE clients (
                         id BIGSERIAL PRIMARY KEY,
                         user_id BIGINT NOT NULL UNIQUE,
                         first_name VARCHAR(255),
                         last_name VARCHAR(255),
                         phone_number VARCHAR(50) UNIQUE,
                         address TEXT,
                         date_of_birth VARCHAR(10),
                         identification_number VARCHAR(100) UNIQUE,
                         status VARCHAR(20) DEFAULT 'ACTIVE',
                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX idx_clients_user_id ON clients(user_id);
CREATE INDEX idx_clients_status ON clients(status);
CREATE INDEX idx_clients_phone_number ON clients(phone_number);
CREATE INDEX idx_clients_identification_number ON clients(identification_number);