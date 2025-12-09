package com.coopcredit.creditapplication.infrastructure.mappers;

import com.coopcredit.creditapplication.domain.model.RiskEvaluation;
import com.coopcredit.creditapplication.infrastructure.entities.RiskEvaluationEntity;
import org.springframework.stereotype.Component;

@Component
public class RiskEvaluationMapper {
    
    public RiskEvaluation toDomain(RiskEvaluationEntity entity) {
        if (entity == null) return null;
        
        RiskEvaluation evaluation = new RiskEvaluation();
        evaluation.setId(entity.getId());
        evaluation.setCreditApplicationId(entity.getCreditApplicationId());
        evaluation.setScore(entity.getScore());
        evaluation.setRiskLevel(entity.getRiskLevel());
        evaluation.setPaymentToIncomeRatio(entity.getPaymentToIncomeRatio());
        evaluation.setMeetsSeniority(entity.isMeetsSeniority());
        evaluation.setMeetsMaxAmount(entity.isMeetsMaxAmount());
        evaluation.setFinalDecision(entity.getFinalDecision());
        evaluation.setReason(entity.getReason());
        evaluation.setRiskCentralDetail(entity.getRiskCentralDetail());
        evaluation.setCreatedAt(entity.getCreatedAt());
        return evaluation;
    }
    
    public RiskEvaluationEntity toEntity(RiskEvaluation domain) {
        if (domain == null) return null;
        
        RiskEvaluationEntity entity = new RiskEvaluationEntity();
        entity.setId(domain.getId());
        entity.setCreditApplicationId(domain.getCreditApplicationId());
        entity.setScore(domain.getScore());
        entity.setRiskLevel(domain.getRiskLevel());
        entity.setPaymentToIncomeRatio(domain.getPaymentToIncomeRatio());
        entity.setMeetsSeniority(domain.isMeetsSeniority());
        entity.setMeetsMaxAmount(domain.isMeetsMaxAmount());
        entity.setFinalDecision(domain.getFinalDecision());
        entity.setReason(domain.getReason());
        entity.setRiskCentralDetail(domain.getRiskCentralDetail());
        return entity;
    }
}