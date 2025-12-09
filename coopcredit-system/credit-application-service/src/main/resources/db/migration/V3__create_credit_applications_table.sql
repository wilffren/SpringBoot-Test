-- V3__create_credit_applications_table.sql
-- Table for credit applications (solicitudes de crédito)

CREATE TABLE credit_applications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    requested_amount DECIMAL(15, 2) NOT NULL,
    term_months INT NOT NULL,
    proposed_rate DECIMAL(5, 4) NOT NULL,
    application_date DATE NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_credit_applications_member_id (member_id),
    INDEX idx_credit_applications_status (status),
    INDEX idx_credit_applications_date (application_date),
    
    CONSTRAINT chk_credit_applications_amount_positive CHECK (requested_amount > 0),
    CONSTRAINT chk_credit_applications_term_positive CHECK (term_months > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;