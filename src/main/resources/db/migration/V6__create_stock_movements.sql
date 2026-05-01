CREATE TABLE stock_movements (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT REFERENCES products(id),
    type VARCHAR(20) NOT NULL,
    quantity INTEGER NOT NULL,
    reason VARCHAR(150),
    order_id BIGINT REFERENCES orders(id),
    created_at TIMESTAMP NOT NULL
);