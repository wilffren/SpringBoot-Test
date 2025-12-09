package com.coopcredit.creditapplication.domain.ports.out;

import com.coopcredit.creditapplication.domain.model.RiskEvaluation;
import java.util.Optional;

/**
 * Port for risk evaluation repository operations.
 */
public interface RiskEvaluationRepositoryPort {
    
    RiskEvaluation save(RiskEvaluation riskEvaluation);
    
    Optional<RiskEvaluation> findById(Long id);
    
    Optional<RiskEvaluation> findByCreditApplicationId(Long creditApplicationId);
    
    boolean existsByCreditApplicationId(Long creditApplicationId);
    
    void deleteById(Long id);
}