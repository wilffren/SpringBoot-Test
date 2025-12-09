package com.coopcredit.creditapplication.domain.ports.in;

import com.coopcredit.creditapplication.domain.model.Member;
import com.coopcredit.creditapplication.domain.model.enums.MemberStatus;
import java.math.BigDecimal;

/**
 * Port for updating a member use case.
 */
public interface UpdateMemberUseCase {
    
    record UpdateMemberCommand(
        Long id,
        String name,
        BigDecimal salary,
        MemberStatus status
    ) {}
    
    Member execute(UpdateMemberCommand command);
}