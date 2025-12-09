package com.coopcredit.creditapplication.domain.ports.in;

import com.coopcredit.creditapplication.domain.model.RiskEvaluation;

/**
 * Port for evaluating a credit application use case.
 */
public interface EvaluateCreditApplicationUseCase {
    
    RiskEvaluation execute(Long creditApplicationId);
}