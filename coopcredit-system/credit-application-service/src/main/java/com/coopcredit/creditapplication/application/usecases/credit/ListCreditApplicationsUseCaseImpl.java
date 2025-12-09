package com.coopcredit.creditapplication.application.usecases.credit;

import com.coopcredit.creditapplication.domain.model.CreditApplication;
import com.coopcredit.creditapplication.domain.model.enums.ApplicationStatus;
import com.coopcredit.creditapplication.domain.ports.in.ListCreditApplicationsUseCase;
import com.coopcredit.creditapplication.domain.ports.out.CreditApplicationRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ListCreditApplicationsUseCaseImpl implements ListCreditApplicationsUseCase {
    
    private final CreditApplicationRepositoryPort creditApplicationRepository;
    
    public ListCreditApplicationsUseCaseImpl(CreditApplicationRepositoryPort creditApplicationRepository) {
        this.creditApplicationRepository = creditApplicationRepository;
    }
    
    @Override
    public List<CreditApplication> execute() {
        return creditApplicationRepository.findAll();
    }
    
    @Override
    public List<CreditApplication> executeByMemberId(Long memberId) {
        return creditApplicationRepository.findByMemberId(memberId);
    }
    
    @Override
    public List<CreditApplication> executeByStatus(ApplicationStatus status) {
        return creditApplicationRepository.findByStatus(status);
    }
}