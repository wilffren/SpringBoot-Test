-- V6__insert_initial_data.sql
-- Insert initial data for testing

-- Insert admin user (password: admin123 - BCrypt encoded)
INSERT INTO users (username, password, role, enabled) VALUES
    ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt8BKgu', 'ROLE_ADMIN', TRUE),
    ('analyst1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt8BKgu', 'ROLE_ANALYST', TRUE);

-- Insert sample members
INSERT INTO members (document, name, salary, affiliation_date, status) VALUES
    ('1234567890', 'Juan Carlos Pérez', 5000000.00, '2020-01-15', 'ACTIVE'),
    ('0987654321', 'María García López', 3500000.00, '2021-06-20', 'ACTIVE'),
    ('1122334455', 'Carlos Rodríguez', 8000000.00, '2019-03-10', 'ACTIVE');

-- Insert sample credit applications
INSERT INTO credit_applications (member_id, requested_amount, term_months, proposed_rate, application_date, status) VALUES
    (1, 10000000.00, 24, 0.0150, '2024-01-10', 'PENDING'),
    (2, 5000000.00, 12, 0.0180, '2024-01-12', 'PENDING');