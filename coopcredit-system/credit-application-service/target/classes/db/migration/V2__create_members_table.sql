-- V2__create_members_table.sql
-- Table for cooperative members (afiliados)

CREATE TABLE members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    document VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    salary DECIMAL(15, 2) NOT NULL,
    affiliation_date DATE NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    user_id BIGINT UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_members_document (document),
    INDEX idx_members_status (status),
    INDEX idx_members_affiliation_date (affiliation_date),
    
    CONSTRAINT chk_members_salary_positive CHECK (salary > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;