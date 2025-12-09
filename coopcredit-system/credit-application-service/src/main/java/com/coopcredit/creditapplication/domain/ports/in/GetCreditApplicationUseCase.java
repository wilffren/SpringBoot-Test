package com.coopcredit.creditapplication.domain.ports.in;

import com.coopcredit.creditapplication.domain.model.CreditApplication;

/**
 * Port for retrieving a single credit application use case.
 */
public interface GetCreditApplicationUseCase {
    
    CreditApplication execute(Long id);
}