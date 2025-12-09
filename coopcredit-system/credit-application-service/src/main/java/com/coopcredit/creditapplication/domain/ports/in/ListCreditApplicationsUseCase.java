package com.coopcredit.creditapplication.domain.ports.in;

import com.coopcredit.creditapplication.domain.model.CreditApplication;
import com.coopcredit.creditapplication.domain.model.enums.ApplicationStatus;
import java.util.List;

/**
 * Port for listing credit applications use case.
 */
public interface ListCreditApplicationsUseCase {
    
    List<CreditApplication> execute();
    
    List<CreditApplication> executeByMemberId(Long memberId);
    
    List<CreditApplication> executeByStatus(ApplicationStatus status);
}