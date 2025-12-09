package com.coopcredit.creditapplication.domain.model;

import com.coopcredit.creditapplication.domain.model.enums.ApplicationStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Domain model representing a credit application (solicitud de crédito).
 */
public class CreditApplication {
    
    private Long id;
    private Long memberId;
    private BigDecimal requestedAmount;
    private Integer termMonths;
    private BigDecimal proposedRate;
    private LocalDate applicationDate;
    private ApplicationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Associated entities (loaded when needed)
    private Member member;
    private RiskEvaluation riskEvaluation;
    
    public CreditApplication() {}
    
    public CreditApplication(Long id, Long memberId, BigDecimal requestedAmount,
                             Integer termMonths, BigDecimal proposedRate,
                             LocalDate applicationDate, ApplicationStatus status) {
        this.id = id;
        this.memberId = memberId;
        this.requestedAmount = requestedAmount;
        this.termMonths = termMonths;
        this.proposedRate = proposedRate;
        this.applicationDate = applicationDate;
        this.status = status;
    }
    
    // Business methods
    public BigDecimal calculateMonthlyPayment() {
        if (requestedAmount == null || termMonths == null || proposedRate == null) {
            return BigDecimal.ZERO;
        }
        // Simple calculation: (principal * (1 + rate * months)) / months
        BigDecimal totalInterest = requestedAmount.multiply(proposedRate).multiply(BigDecimal.valueOf(termMonths));
        BigDecimal totalAmount = requestedAmount.add(totalInterest);
        return totalAmount.divide(BigDecimal.valueOf(termMonths), 2, java.math.RoundingMode.HALF_UP);
    }
    
    public boolean isPending() {
        return ApplicationStatus.PENDING.equals(status);
    }
    
    public void approve() {
        this.status = ApplicationStatus.APPROVED;
    }
    
    public void reject() {
        this.status = ApplicationStatus.REJECTED;
    }
    
    // Builder pattern
    public static CreditApplicationBuilder builder() {
        return new CreditApplicationBuilder();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    
    public BigDecimal getRequestedAmount() { return requestedAmount; }
    public void setRequestedAmount(BigDecimal requestedAmount) { this.requestedAmount = requestedAmount; }
    
    public Integer getTermMonths() { return termMonths; }
    public void setTermMonths(Integer termMonths) { this.termMonths = termMonths; }
    
    public BigDecimal getProposedRate() { return proposedRate; }
    public void setProposedRate(BigDecimal proposedRate) { this.proposedRate = proposedRate; }
    
    public LocalDate getApplicationDate() { return applicationDate; }
    public void setApplicationDate(LocalDate applicationDate) { this.applicationDate = applicationDate; }
    
    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public Member getMember() { return member; }
    public void setMember(Member member) { this.member = member; }
    
    public RiskEvaluation getRiskEvaluation() { return riskEvaluation; }
    public void setRiskEvaluation(RiskEvaluation riskEvaluation) { this.riskEvaluation = riskEvaluation; }
    
    public static class CreditApplicationBuilder {
        private Long id;
        private Long memberId;
        private BigDecimal requestedAmount;
        private Integer termMonths;
        private BigDecimal proposedRate;
        private LocalDate applicationDate;
        private ApplicationStatus status = ApplicationStatus.PENDING;
        
        public CreditApplicationBuilder id(Long id) { this.id = id; return this; }
        public CreditApplicationBuilder memberId(Long memberId) { this.memberId = memberId; return this; }
        public CreditApplicationBuilder requestedAmount(BigDecimal requestedAmount) { this.requestedAmount = requestedAmount; return this; }
        public CreditApplicationBuilder termMonths(Integer termMonths) { this.termMonths = termMonths; return this; }
        public CreditApplicationBuilder proposedRate(BigDecimal proposedRate) { this.proposedRate = proposedRate; return this; }
        public CreditApplicationBuilder applicationDate(LocalDate applicationDate) { this.applicationDate = applicationDate; return this; }
        public CreditApplicationBuilder status(ApplicationStatus status) { this.status = status; return this; }
        
        public CreditApplication build() {
            return new CreditApplication(id, memberId, requestedAmount, termMonths, proposedRate, applicationDate, status);
        }
    }
}