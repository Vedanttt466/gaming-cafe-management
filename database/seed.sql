USE gaming_cafe_db;

-- Default users for first login (CHANGE THESE PASSWORDS IN PRODUCTION)
-- Owner   -> email: owner@cafe.com    password: Owner@123
-- Staff   -> email: staff@cafe.com    password: Staff@123
-- Customer-> email: customer@cafe.com password: Customer@123

INSERT INTO users (name, email, phone, password, role, enabled) VALUES
('Cafe Owner', 'owner@cafe.com', '9999900001', '$2b$10$GAkSEXF3Qdl2ZgNDmabr/OeV3PgzIDT9Jpwt6UyEF91fnSZ7zkFBe', 'OWNER', TRUE),
('Front Desk Staff', 'staff@cafe.com', '9999900002', '$2b$10$AEwbIuJj5Wgf7ioNM7wso.EuyJ6VBOw2Xm/2coQRcDzwpYoCgmeom', 'STAFF', TRUE),
('Test Customer', 'customer@cafe.com', '9999900003', '$2b$10$FvBvzww8CRwZTfaDgJUT6u54KtOB41lYkf2.pB/dTpzw9sYznN5d2', 'CUSTOMER', TRUE);
