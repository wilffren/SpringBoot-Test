package com.coopcredit.creditapplication.application.usecases.credit;

import com.coopcredit.creditapplication.domain.exception.NotFoundException;
import com.coopcredit.creditapplication.domain.model.CreditApplication;
import com.coopcredit.creditapplication.domain.ports.in.GetCreditApplicationUseCase;
import com.coopcredit.creditapplication.domain.ports.out.CreditApplicationRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetCreditApplicationUseCaseImpl implements GetCreditApplicationUseCase {
    
    private final CreditApplicationRepositoryPort creditApplicationRepository;
    
    public GetCreditApplicationUseCaseImpl(CreditApplicationRepositoryPort creditApplicationRepository) {
        this.creditApplicationRepository = creditApplicationRepository;
    }
    
    @Override
    public CreditApplication execute(Long id) {
        return creditApplicationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("CreditApplication", "id", id));
    }
}