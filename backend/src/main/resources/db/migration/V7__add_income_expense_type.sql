ALTER TABLE transactions
ADD COLUMN entry_type VARCHAR(20);

UPDATE transactions
SET entry_type = CASE
    WHEN transaction_type = 'BUY' THEN 'EXPENSE'
    WHEN transaction_type = 'SELL' THEN 'INCOME'
END;

ALTER TABLE transactions
ALTER COLUMN entry_type SET NOT NULL;

ALTER TABLE transactions
ADD CONSTRAINT chk_transaction_entry_type
CHECK (entry_type IN ('INCOME', 'EXPENSE'));

CREATE INDEX idx_transactions_entry_type
ON transactions(entry_type);