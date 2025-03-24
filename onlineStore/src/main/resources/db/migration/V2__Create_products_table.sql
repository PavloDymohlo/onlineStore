CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(100) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    quantity INTEGER NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

INSERT INTO products (name, category, price, quantity, deleted)
SELECT 'iPhone 14 Pro', 'Phone', 1000.00, 15, FALSE
WHERE NOT EXISTS (
    SELECT 1 FROM products WHERE name = 'iPhone 14 Pro'
);

INSERT INTO products (name, category, price, quantity, deleted)
SELECT 'Samsung Galaxy S23', 'Phone', 900.00, 20, FALSE
WHERE NOT EXISTS (
    SELECT 1 FROM products WHERE name = 'Samsung Galaxy S23'
);

INSERT INTO products (name, category, price, quantity, deleted)
SELECT 'MacBook Pro 16"', 'Laptop', 2500.00, 8, FALSE
WHERE NOT EXISTS (
    SELECT 1 FROM products WHERE name = 'MacBook Pro 16"'
);

INSERT INTO products (name, category, price, quantity, deleted)
SELECT 'Dell XPS 15', 'Laptop', 1800.00, 12, FALSE
WHERE NOT EXISTS (
    SELECT 1 FROM products WHERE name = 'Dell XPS 15'
);

INSERT INTO products (name, category, price, quantity, deleted)
SELECT 'Sony Bravia 55"', 'TV', 1200.00, 10, FALSE
WHERE NOT EXISTS (
    SELECT 1 FROM products WHERE name = 'Sony Bravia 55"'
);

INSERT INTO products (name, category, price, quantity, deleted)
SELECT 'LG OLED 65"', 'TV', 1700.00, 7, FALSE
WHERE NOT EXISTS (
    SELECT 1 FROM products WHERE name = 'LG OLED 65"'
);