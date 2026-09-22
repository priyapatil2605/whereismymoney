ALTER TABLE holdings
    ADD COLUMN IF NOT EXISTS current_price NUMERIC(20,8),
    ADD COLUMN IF NOT EXISTS last_price_updated_at TIMESTAMPTZ;

CREATE INDEX IF NOT EXISTS idx_holdings_portfolio_id
    ON holdings(portfolio_id);

CREATE INDEX IF NOT EXISTS idx_holdings_asset_id
    ON holdings(asset_id);