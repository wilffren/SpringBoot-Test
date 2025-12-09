package com.coopcredit.creditapplication.infrastructure.entities;

import com.coopcredit.creditapplication.domain.model.enums.FinalDecision;
import com.coopcredit.creditapplication.domain.model.enums.RiskLevel;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "risk_evaluations")
public class RiskEvaluationEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "credit_application_id", nullable = false, unique = true)
    private Long creditApplicationId;
    
    @Column(nullable = false)
    private Integer score;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false)
    private RiskLevel riskLevel;
    
    @Column(name = "payment_to_income_ratio", nullable = false, precision = 5, scale = 4)
    private BigDecimal paymentToIncomeRatio;
    
    @Column(name = "meets_seniority", nullable = false)
    private boolean meetsSeniority;
    
    @Column(name = "meets_max_amount", nullable = false)
    private boolean meetsMaxAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "final_decision", nullable = false)
    private FinalDecision finalDecision;
    
    @Column(columnDefinition = "TEXT")
    private String reason;
    
    @Column(name = "risk_central_detail", columnDefinition = "TEXT")
    private String riskCentralDetail;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
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
}