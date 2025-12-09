package com.coopcredit.creditapplication.domain.ports.out;

import com.coopcredit.creditapplication.domain.model.enums.RiskLevel;

/**
 * Port for external risk central service operations.
 */
public interface RiskCentralPort {
    
    record RiskCentralRequest(String document, java.math.BigDecimal requestedAmount) {}
    
    record RiskCentralResponse(
        Integer score,
        RiskLevel riskLevel,
        String detail
    ) {}
    
    RiskCentralResponse evaluateRisk(RiskCentralRequest request);
}