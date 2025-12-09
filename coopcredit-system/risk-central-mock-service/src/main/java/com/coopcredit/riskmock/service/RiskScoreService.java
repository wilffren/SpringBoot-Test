package com.coopcredit.riskmock.service;

import com.coopcredit.riskmock.dto.RiskEvaluationRequest;
import com.coopcredit.riskmock.dto.RiskEvaluationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RiskScoreService {
    
    private static final Logger log = LoggerFactory.getLogger(RiskScoreService.class);
    
    /**
     * Simulates a risk score calculation based on document and amount.
     * In a real scenario, this would call external credit bureaus and scoring systems.
     */
    public RiskEvaluationResponse evaluateRisk(RiskEvaluationRequest request) {
        log.info("Evaluating risk for document: {}, amount: {}", 
                request.document(), request.requestedAmount());
        
        // Generate a score based on document characteristics (simulation)
        int score = calculateScore(request.document(), request.requestedAmount());
        String riskLevel = determineRiskLevel(score);
        String detail = generateDetail(score, riskLevel);
        
        log.info("Risk evaluation complete - Score: {}, Level: {}", score, riskLevel);
        
        return new RiskEvaluationResponse(score, riskLevel, detail);
    }
    
    private int calculateScore(String document, java.math.BigDecimal amount) {
        // Use document hash as seed for consistent results per document
        // Same document will always return the same score
        int seed = document != null ? Math.abs(document.hashCode()) : 0;
        
        // Generate deterministic score based on seed (300-950 range per spec)
        int baseScore = 300 + (seed % 651); // 651 = 950 - 300 + 1
        
        // Ensure score is within valid range (300-950)
        return Math.max(300, Math.min(950, baseScore));
    }
    
    private String determineRiskLevel(int score) {
        // Per specification:
        // 300–500 → HIGH RISK
        // 501–700 → MEDIUM RISK  
        // 701–950 → LOW RISK
        if (score >= 701) {
            return "LOW";
        } else if (score >= 501) {
            return "MEDIUM";
        } else {
            return "HIGH";
        }
    }
    
    private String generateDetail(int score, String riskLevel) {
        return switch (riskLevel) {
            case "LOW" -> "Excellent credit history. Score of " + score + " indicates low risk profile.";
            case "MEDIUM" -> "Moderate credit history. Score of " + score + " requires standard evaluation.";
            case "HIGH" -> "Credit concerns detected. Score of " + score + " indicates elevated risk.";
            default -> "Unable to determine risk profile.";
        };
    }
}