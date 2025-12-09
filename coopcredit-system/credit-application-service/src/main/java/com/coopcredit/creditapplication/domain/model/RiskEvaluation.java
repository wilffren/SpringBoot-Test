package com.coopcredit.creditapplication.domain.model;

import com.coopcredit.creditapplication.domain.model.enums.FinalDecision;
import com.coopcredit.creditapplication.domain.model.enums.RiskLevel;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Domain model representing a risk evaluation for a credit application.
 */
public class RiskEvaluation {
    
    private Long id;
    private Long creditApplicationId;
    private Integer score;
    private RiskLevel riskLevel;
    private BigDecimal paymentToIncomeRatio;
    private boolean meetsSeniority;
    private boolean meetsMaxAmount;
    private FinalDecision finalDecision;
    private String reason;
    private String riskCentralDetail;
    private LocalDateTime createdAt;
    
    public RiskEvaluation() {}
    
    public RiskEvaluation(Long id, Long creditApplicationId, Integer score, RiskLevel riskLevel,
                          BigDecimal paymentToIncomeRatio, boolean meetsSeniority, boolean meetsMaxAmount,
                          FinalDecision finalDecision, String reason, String riskCentralDetail) {
        this.id = id;
        this.creditApplicationId = creditApplicationId;
        this.score = score;
        this.riskLevel = riskLevel;
        this.paymentToIncomeRatio = paymentToIncomeRatio;
        this.meetsSeniority = meetsSeniority;
        this.meetsMaxAmount = meetsMaxAmount;
        this.finalDecision = finalDecision;
        this.reason = reason;
        this.riskCentralDetail = riskCentralDetail;
    }
    
    // Business methods
    public boolean isApproved() {
        return FinalDecision.APPROVED.equals(finalDecision);
    }
    
    public boolean passesAllChecks() {
        return meetsSeniority && meetsMaxAmount && 
               (RiskLevel.LOW.equals(riskLevel) || RiskLevel.MEDIUM.equals(riskLevel));
    }
    
    // Builder pattern
    public static RiskEvaluationBuilder builder() {
        return new RiskEvaluationBuilder();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getCreditApplicationId() { return creditApplicationId; }
    public void setCreditApplicationId(Long creditApplicationId) { this.creditApplicationId = creditApplicationId; }
    
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    
    public RiskLevel getRiskLevel() { return riskLevel; }
    public void setRiskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; }
    
    public BigDecimal getPaymentToIncomeRatio() { return paymentToIncomeRatio; }
    public void setPaymentToIncomeRatio(BigDecimal paymentToIncomeRatio) { this.paymentToIncomeRatio = paymentToIncomeRatio; }
    
    public boolean isMeetsSeniority() { return meetsSeniority; }
    public void setMeetsSeniority(boolean meetsSeniority) { this.meetsSeniority = meetsSeniority; }
    
    public boolean isMeetsMaxAmount() { return meetsMaxAmount; }
    public void setMeetsMaxAmount(boolean meetsMaxAmount) { this.meetsMaxAmount = meetsMaxAmount; }
    
    public FinalDecision getFinalDecision() { return finalDecision; }
    public void setFinalDecision(FinalDecision finalDecision) { this.finalDecision = finalDecision; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    
    public String getRiskCentralDetail() { return riskCentralDetail; }
    public void setRiskCentralDetail(String riskCentralDetail) { this.riskCentralDetail = riskCentralDetail; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public static class RiskEvaluationBuilder {
        private Long id;
        private Long creditApplicationId;
        private Integer score;
        private RiskLevel riskLevel;
        private BigDecimal paymentToIncomeRatio;
        private boolean meetsSeniority;
        private boolean meetsMaxAmount;
        private FinalDecision finalDecision;
        private String reason;
        private String riskCentralDetail;
        
        public RiskEvaluationBuilder id(Long id) { this.id = id; return this; }
        public RiskEvaluationBuilder creditApplicationId(Long creditApplicationId) { this.creditApplicationId = creditApplicationId; return this; }
        public RiskEvaluationBuilder score(Integer score) { this.score = score; return this; }
        public RiskEvaluationBuilder riskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; return this; }
        public RiskEvaluationBuilder paymentToIncomeRatio(BigDecimal paymentToIncomeRatio) { this.paymentToIncomeRatio = paymentToIncomeRatio; return this; }
        public RiskEvaluationBuilder meetsSeniority(boolean meetsSeniority) { this.meetsSeniority = meetsSeniority; return this; }
        public RiskEvaluationBuilder meetsMaxAmount(boolean meetsMaxAmount) { this.meetsMaxAmount = meetsMaxAmount; return this; }
        public RiskEvaluationBuilder finalDecision(FinalDecision finalDecision) { this.finalDecision = finalDecision; return this; }
        public RiskEvaluationBuilder reason(String reason) { this.reason = reason; return this; }
        public RiskEvaluationBuilder riskCentralDetail(String riskCentralDetail) { this.riskCentralDetail = riskCentralDetail; return this; }
        
        public RiskEvaluation build() {
            return new RiskEvaluation(id, creditApplicationId, score, riskLevel, paymentToIncomeRatio, 
                                      meetsSeniority, meetsMaxAmount, finalDecision, reason, riskCentralDetail);
        }
    }
}