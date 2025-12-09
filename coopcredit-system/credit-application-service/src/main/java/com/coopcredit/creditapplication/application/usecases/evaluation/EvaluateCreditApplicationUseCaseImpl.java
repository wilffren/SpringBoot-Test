package com.coopcredit.creditapplication.application.usecases.evaluation;

import com.coopcredit.creditapplication.domain.exception.BusinessRuleException;
import com.coopcredit.creditapplication.domain.exception.NotFoundException;
import com.coopcredit.creditapplication.domain.model.CreditApplication;
import com.coopcredit.creditapplication.domain.model.Member;
import com.coopcredit.creditapplication.domain.model.RiskEvaluation;
import com.coopcredit.creditapplication.domain.model.enums.FinalDecision;
import com.coopcredit.creditapplication.domain.model.enums.RiskLevel;
import com.coopcredit.creditapplication.domain.ports.in.EvaluateCreditApplicationUseCase;
import com.coopcredit.creditapplication.domain.ports.out.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class EvaluateCreditApplicationUseCaseImpl implements EvaluateCreditApplicationUseCase {
    
    private static final int MIN_SENIORITY_MONTHS = 6;
    private static final BigDecimal MAX_PAYMENT_TO_INCOME_RATIO = new BigDecimal("0.40");
    
    private final CreditApplicationRepositoryPort creditApplicationRepository;
    private final MemberRepositoryPort memberRepository;
    private final RiskEvaluationRepositoryPort riskEvaluationRepository;
    private final RiskCentralPort riskCentralPort;
    
    public EvaluateCreditApplicationUseCaseImpl(
            CreditApplicationRepositoryPort creditApplicationRepository,
            MemberRepositoryPort memberRepository,
            RiskEvaluationRepositoryPort riskEvaluationRepository,
            RiskCentralPort riskCentralPort) {
        this.creditApplicationRepository = creditApplicationRepository;
        this.memberRepository = memberRepository;
        this.riskEvaluationRepository = riskEvaluationRepository;
        this.riskCentralPort = riskCentralPort;
    }
    
    @Override
    public RiskEvaluation execute(Long creditApplicationId) {
        // Check if already evaluated
        if (riskEvaluationRepository.existsByCreditApplicationId(creditApplicationId)) {
            throw new BusinessRuleException("ALREADY_EVALUATED", 
                    "Credit application has already been evaluated");
        }
        
        // Get credit application
        CreditApplication application = creditApplicationRepository.findById(creditApplicationId)
                .orElseThrow(() -> new NotFoundException("CreditApplication", "id", creditApplicationId));
        
        if (!application.isPending()) {
            throw new BusinessRuleException("NOT_PENDING", 
                    "Credit application is not in PENDING status");
        }
        
        // Get member
        Member member = memberRepository.findById(application.getMemberId())
                .orElseThrow(() -> new NotFoundException("Member", "id", application.getMemberId()));
        
        // Call external risk central service
        RiskCentralPort.RiskCentralResponse riskResponse = riskCentralPort.evaluateRisk(
                new RiskCentralPort.RiskCentralRequest(member.getDocument(), application.getRequestedAmount()));
        
        // Calculate payment to income ratio
        BigDecimal monthlyPayment = application.calculateMonthlyPayment();
        BigDecimal paymentToIncomeRatio = monthlyPayment.divide(member.getSalary(), 4, RoundingMode.HALF_UP);
        
        // Check business rules
        boolean meetsSeniority = member.getSeniorityInMonths() >= MIN_SENIORITY_MONTHS;
        boolean meetsMaxAmount = application.getRequestedAmount().compareTo(member.getMaxCreditAmount()) <= 0;
        boolean meetsPaymentRatio = paymentToIncomeRatio.compareTo(MAX_PAYMENT_TO_INCOME_RATIO) <= 0;
        
        // Determine final decision
        List<String> rejectionReasons = new ArrayList<>();
        
        if (!meetsSeniority) {
            rejectionReasons.add("Insufficient seniority (minimum 6 months required)");
        }
        if (!meetsMaxAmount) {
            rejectionReasons.add("Requested amount exceeds maximum allowed (4x salary)");
        }
        if (!meetsPaymentRatio) {
            rejectionReasons.add("Payment to income ratio exceeds 40%");
        }
        if (RiskLevel.HIGH.equals(riskResponse.riskLevel())) {
            rejectionReasons.add("High risk score from central risk service");
        }
        
        FinalDecision decision = rejectionReasons.isEmpty() ? FinalDecision.APPROVED : FinalDecision.REJECTED;
        String reason = rejectionReasons.isEmpty() ? "All criteria met" : String.join("; ", rejectionReasons);
        
        // Create and save evaluation
        RiskEvaluation evaluation = RiskEvaluation.builder()
                .creditApplicationId(creditApplicationId)
                .score(riskResponse.score())
                .riskLevel(riskResponse.riskLevel())
                .paymentToIncomeRatio(paymentToIncomeRatio)
                .meetsSeniority(meetsSeniority)
                .meetsMaxAmount(meetsMaxAmount)
                .finalDecision(decision)
                .reason(reason)
                .riskCentralDetail(riskResponse.detail())
                .build();
        
        RiskEvaluation savedEvaluation = riskEvaluationRepository.save(evaluation);
        
        // Update credit application status
        if (decision == FinalDecision.APPROVED) {
            application.approve();
        } else {
            application.reject();
        }
        creditApplicationRepository.save(application);
        
        return savedEvaluation;
    }
}