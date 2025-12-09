package com.coopcredit.creditapplication.application.usecases.credit;

import com.coopcredit.creditapplication.domain.exception.BusinessRuleException;
import com.coopcredit.creditapplication.domain.exception.NotFoundException;
import com.coopcredit.creditapplication.domain.model.CreditApplication;
import com.coopcredit.creditapplication.domain.model.Member;
import com.coopcredit.creditapplication.domain.model.enums.ApplicationStatus;
import com.coopcredit.creditapplication.domain.ports.in.CreateCreditApplicationUseCase;
import com.coopcredit.creditapplication.domain.ports.out.CreditApplicationRepositoryPort;
import com.coopcredit.creditapplication.domain.ports.out.MemberRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional
public class CreateCreditApplicationUseCaseImpl implements CreateCreditApplicationUseCase {
    
    private final CreditApplicationRepositoryPort creditApplicationRepository;
    private final MemberRepositoryPort memberRepository;
    
    public CreateCreditApplicationUseCaseImpl(
            CreditApplicationRepositoryPort creditApplicationRepository,
            MemberRepositoryPort memberRepository) {
        this.creditApplicationRepository = creditApplicationRepository;
        this.memberRepository = memberRepository;
    }
    
    @Override
    public CreditApplication execute(CreateCreditApplicationCommand command) {
        // Validate member exists and is active
        Member member = memberRepository.findById(command.memberId())
                .orElseThrow(() -> new NotFoundException("Member", "id", command.memberId()));
        
        if (!member.isActive()) {
            throw new BusinessRuleException("MEMBER_INACTIVE", "Member is not active");
        }
        
        // Business rule: minimum 6 months of seniority
        if (member.getSeniorityInMonths() < 6) {
            throw new BusinessRuleException("INSUFFICIENT_SENIORITY", 
                    "Member must have at least 6 months of seniority");
        }
        
        CreditApplication creditApplication = CreditApplication.builder()
                .memberId(command.memberId())
                .requestedAmount(command.requestedAmount())
                .termMonths(command.termMonths())
                .proposedRate(command.proposedRate())
                .applicationDate(LocalDate.now())
                .status(ApplicationStatus.PENDING)
                .build();
        
        return creditApplicationRepository.save(creditApplication);
    }
}