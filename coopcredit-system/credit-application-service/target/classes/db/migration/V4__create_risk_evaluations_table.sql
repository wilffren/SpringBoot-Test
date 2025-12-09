-- V4__create_risk_evaluations_table.sql
-- Table for risk evaluations (evaluaciones de riesgo)

CREATE TABLE risk_evaluations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    credit_application_id BIGINT NOT NULL UNIQUE,
    score INT NOT NULL,
    risk_level ENUM('LOW', 'MEDIUM', 'HIGH') NOT NULL,
    payment_to_income_ratio DECIMAL(5, 4) NOT NULL,
    meets_seniority BOOLEAN NOT NULL,
    meets_max_amount BOOLEAN NOT NULL,
    final_decision ENUM('APPROVED', 'REJECTED') NOT NULL,
    reason TEXT,
    risk_central_detail TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_risk_evaluations_credit_application_id (credit_application_id),
    INDEX idx_risk_evaluations_risk_level (risk_level),
    INDEX idx_risk_evaluations_final_decision (final_decision),
    
    CONSTRAINT chk_risk_evaluations_score_range CHECK (score >= 300 AND score <= 950)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;