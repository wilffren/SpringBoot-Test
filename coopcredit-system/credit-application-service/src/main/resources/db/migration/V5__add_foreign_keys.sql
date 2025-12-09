-- V5__add_foreign_keys.sql
-- Add foreign key constraints

-- Member to User relationship (1:1)
ALTER TABLE members
    ADD CONSTRAINT fk_members_user_id 
    FOREIGN KEY (user_id) REFERENCES users(id) 
    ON DELETE SET NULL ON UPDATE CASCADE;

-- Credit Application to Member relationship (N:1)
ALTER TABLE credit_applications
    ADD CONSTRAINT fk_credit_applications_member_id 
    FOREIGN KEY (member_id) REFERENCES members(id) 
    ON DELETE RESTRICT ON UPDATE CASCADE;

-- Risk Evaluation to Credit Application relationship (1:1)
ALTER TABLE risk_evaluations
    ADD CONSTRAINT fk_risk_evaluations_credit_application_id 
    FOREIGN KEY (credit_application_id) REFERENCES credit_applications(id) 
    ON DELETE CASCADE ON UPDATE CASCADE;