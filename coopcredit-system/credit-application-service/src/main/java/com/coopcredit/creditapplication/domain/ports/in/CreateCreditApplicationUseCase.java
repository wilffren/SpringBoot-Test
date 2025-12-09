package com.coopcredit.creditapplication.domain.ports.in;

import com.coopcredit.creditapplication.domain.model.CreditApplication;
import java.math.BigDecimal;

/**
 * Port for creating a new credit application use case.
 */
public interface CreateCreditApplicationUseCase {
    
    record CreateCreditApplicationCommand(
        Long memberId,
        BigDecimal requestedAmount,
        Integer termMonths,
        BigDecimal proposedRate
    ) {}
    
    CreditApplication execute(CreateCreditApplicationCommand command);
}