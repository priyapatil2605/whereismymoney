CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    full_name VARCHAR(150) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE portfolios (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_portfolio_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_portfolio_user_name
        UNIQUE (user_id, name)
);


CREATE TABLE assets (
    id BIGSERIAL PRIMARY KEY,
    symbol VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    asset_type VARCHAR(30) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'USD',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_asset_type
        CHECK (asset_type IN ('STOCK', 'ETF', 'MUTUAL_FUND'))
);


CREATE TABLE holdings (
    id BIGSERIAL PRIMARY KEY,
    portfolio_id BIGINT NOT NULL,
    asset_id BIGINT NOT NULL,
    quantity NUMERIC(20,8) NOT NULL DEFAULT 0,
    average_buy_price NUMERIC(20,8) NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_holding_portfolio
        FOREIGN KEY (portfolio_id)
        REFERENCES portfolios(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_holding_asset
        FOREIGN KEY (asset_id)
        REFERENCES assets(id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_holding_quantity
        CHECK (quantity >= 0),

    CONSTRAINT chk_average_buy_price
        CHECK (average_buy_price >= 0),

    CONSTRAINT uq_portfolio_asset
        UNIQUE (portfolio_id, asset_id)
);


CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    portfolio_id BIGINT NOT NULL,
    asset_id BIGINT NOT NULL,
    transaction_type VARCHAR(10) NOT NULL,
    quantity NUMERIC(20,8) NOT NULL,
    price_per_unit NUMERIC(20,8) NOT NULL,
    transaction_date TIMESTAMPTZ NOT NULL,
    total_amount NUMERIC(20,8) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_transaction_portfolio
        FOREIGN KEY (portfolio_id)
        REFERENCES portfolios(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_transaction_asset
        FOREIGN KEY (asset_id)
        REFERENCES assets(id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_transaction_type
        CHECK (transaction_type IN ('BUY', 'SELL')),

    CONSTRAINT chk_transaction_quantity
        CHECK (quantity > 0),

    CONSTRAINT chk_transaction_price
        CHECK (price_per_unit >= 0),

    CONSTRAINT chk_transaction_total
        CHECK (total_amount >= 0)
);


CREATE INDEX idx_portfolios_user_id
    ON portfolios(user_id);

CREATE INDEX idx_holdings_portfolio_id
    ON holdings(portfolio_id);

CREATE INDEX idx_holdings_asset_id
    ON holdings(asset_id);

CREATE INDEX idx_transactions_portfolio_id
    ON transactions(portfolio_id);

CREATE INDEX idx_transactions_asset_id
    ON transactions(asset_id);

CREATE INDEX idx_transactions_date
    ON transactions(transaction_date);