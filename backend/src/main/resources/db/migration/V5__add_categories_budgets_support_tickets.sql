CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE transactions
ADD COLUMN category_id BIGINT;

ALTER TABLE transactions
ADD CONSTRAINT fk_transaction_category
FOREIGN KEY (category_id)
REFERENCES categories(id)
ON DELETE SET NULL;

CREATE INDEX idx_transactions_category_id
ON transactions(category_id);

CREATE TABLE budgets (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category_id BIGINT,
    name VARCHAR(150) NOT NULL,
    amount NUMERIC(20,8) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_budget_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_budget_category
        FOREIGN KEY (category_id)
        REFERENCES categories(id)
        ON DELETE SET NULL,

    CONSTRAINT chk_budget_amount
        CHECK (amount >= 0),

    CONSTRAINT chk_budget_dates
        CHECK (end_date >= start_date)
);

CREATE INDEX idx_budgets_user_id
ON budgets(user_id);

CREATE INDEX idx_budgets_category_id
ON budgets(category_id);

CREATE TABLE support_tickets (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    issue_title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    resolution TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_support_ticket_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_support_ticket_status
        CHECK (status IN (
            'OPEN',
            'IN_PROGRESS',
            'RESOLVED',
            'CLOSED'
        )),

    CONSTRAINT chk_support_ticket_priority
        CHECK (priority IN (
            'LOW',
            'MEDIUM',
            'HIGH',
            'CRITICAL'
        ))
);

CREATE INDEX idx_support_tickets_user_id
ON support_tickets(user_id);

CREATE INDEX idx_support_tickets_status
ON support_tickets(status);

CREATE INDEX idx_support_tickets_priority
ON support_tickets(priority);