package com.coopcredit.creditapplication.domain.ports.in;

import com.coopcredit.creditapplication.domain.model.Member;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Port for creating a new member use case.
 */
public interface CreateMemberUseCase {
    
    record CreateMemberCommand(
        String document,
        String name,
        BigDecimal salary,
        LocalDate affiliationDate
    ) {}
    
    Member execute(CreateMemberCommand command);
}