-- Add low stock threshold to products
ALTER TABLE products ADD COLUMN low_stock_threshold INTEGER NOT NULL DEFAULT 10;

-- Create inventory transactions table
CREATE TABLE inventory_transactions (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT REFERENCES products(id),
    quantity INTEGER NOT NULL,
    type VARCHAR(50) NOT NULL,
    reference VARCHAR(255),
    timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create stock alerts table
CREATE TABLE stock_alerts (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT REFERENCES products(id),
    threshold INTEGER NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Create inventory analytics table
CREATE TABLE inventory_analytics (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT REFERENCES products(id),
    date DATE NOT NULL,
    starting_stock INTEGER,
    ending_stock INTEGER,
    sales_count INTEGER,
    restock_count INTEGER,
    returns_count INTEGER,
    turnover_rate DOUBLE PRECISION,
    days_out_of_stock INTEGER,
    low_stock_incidents INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX idx_inventory_transactions_product ON inventory_transactions(product_id);
CREATE INDEX idx_inventory_transactions_type ON inventory_transactions(type);
CREATE INDEX idx_stock_alerts_product ON stock_alerts(product_id);
CREATE INDEX idx_inventory_analytics_product ON inventory_analytics(product_id);
CREATE INDEX idx_inventory_analytics_date ON inventory_analytics(date);

-- Add trigger for inventory analytics
CREATE OR REPLACE FUNCTION update_inventory_analytics()
RETURNS TRIGGER AS $$
BEGIN
    -- Update or insert analytics record for the current date
    INSERT INTO inventory_analytics (
        product_id,
        date,
        ending_stock,
        sales_count,
        restock_count,
        returns_count,
        turnover_rate,
        low_stock_incidents
    )
    VALUES (
        NEW.product_id,
        CURRENT_DATE,
        NEW.quantity,
        CASE WHEN NEW.type = 'SALE' THEN 1 ELSE 0 END,
        CASE WHEN NEW.type = 'RESTOCK' THEN 1 ELSE 0 END,
        CASE WHEN NEW.type = 'RETURN' THEN 1 ELSE 0 END,
        0, -- Will be updated by a scheduled job
        CASE WHEN NEW.quantity <= (SELECT low_stock_threshold FROM products WHERE id = NEW.product_id) THEN 1 ELSE 0 END
    )
    ON CONFLICT (product_id, date) DO UPDATE
    SET
        ending_stock = EXCLUDED.ending_stock,
        sales_count = inventory_analytics.sales_count + EXCLUDED.sales_count,
        restock_count = inventory_analytics.restock_count + EXCLUDED.restock_count,
        returns_count = inventory_analytics.returns_count + EXCLUDED.returns_count,
        low_stock_incidents = inventory_analytics.low_stock_incidents + EXCLUDED.low_stock_incidents;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER inventory_transaction_analytics
    AFTER INSERT ON inventory_transactions
    FOR EACH ROW
    EXECUTE FUNCTION update_inventory_analytics();