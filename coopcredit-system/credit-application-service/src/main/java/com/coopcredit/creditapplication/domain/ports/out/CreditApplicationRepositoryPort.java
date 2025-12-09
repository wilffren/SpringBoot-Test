package com.coopcredit.creditapplication.domain.ports.out;

import com.coopcredit.creditapplication.domain.model.CreditApplication;
import com.coopcredit.creditapplication.domain.model.enums.ApplicationStatus;
import java.util.List;
import java.util.Optional;

/**
 * Port for credit application repository operations.
 */
public interface CreditApplicationRepositoryPort {
    
    CreditApplication save(CreditApplication creditApplication);
    
    Optional<CreditApplication> findById(Long id);
    
    List<CreditApplication> findAll();
    
    List<CreditApplication> findByMemberId(Long memberId);
    
    List<CreditApplication> findByStatus(ApplicationStatus status);
    
    void deleteById(Long id);
}