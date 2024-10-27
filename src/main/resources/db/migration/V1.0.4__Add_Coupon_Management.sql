-- Create coupons table
CREATE TABLE coupons (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(16) NOT NULL UNIQUE,
    description TEXT NOT NULL,
    discount_type VARCHAR(20) NOT NULL,
    discount_value DECIMAL(10,2) NOT NULL,
    minimum_purchase DECIMAL(10,2),
    maximum_discount DECIMAL(10,2),
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    usage_limit INTEGER NOT NULL,
    usage_count INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Create coupon-category relationship table
CREATE TABLE coupon_categories (
    coupon_id BIGINT REFERENCES coupons(id),
    category_id BIGINT REFERENCES categories(id),
    PRIMARY KEY (coupon_id, category_id)
);

-- Create coupon-product relationship table
CREATE TABLE coupon_products (
    coupon_id BIGINT REFERENCES coupons(id),
    product_id BIGINT REFERENCES products(id),
    PRIMARY KEY (coupon_id, product_id)
);

-- Create indexes
CREATE INDEX idx_coupons_code ON coupons(code);
CREATE INDEX idx_coupons_active ON coupons(active);
CREATE INDEX idx_coupons_dates ON coupons(start_date, end_date);
CREATE INDEX idx_coupon_categories_coupon ON coupon_categories(coupon_id);
CREATE INDEX idx_coupon_categories_category ON coupon_categories(category_id);
CREATE INDEX idx_coupon_products_coupon ON coupon_products(coupon_id);
CREATE INDEX idx_coupon_products_product ON coupon_products(product_id);

-- Add coupon tracking to orders
ALTER TABLE orders ADD COLUMN coupon_code VARCHAR(16);
ALTER TABLE orders ADD COLUMN discount_amount DECIMAL(10,2);