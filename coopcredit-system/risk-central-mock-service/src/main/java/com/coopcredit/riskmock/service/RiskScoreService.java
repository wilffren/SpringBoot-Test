package com.coopcredit.riskmock.service;

import com.coopcredit.riskmock.dto.RiskEvaluationRequest;
import com.coopcredit.riskmock.dto.RiskEvaluationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Random;

@Service
public class RiskScoreService {
    
    private static final Logger log = LoggerFactory.getLogger(RiskScoreService.class);
    private final Random random = new Random();
    
    /**
     * Simulates a risk score calculation based on document and amount.
     * In a real scenario, this would call external credit bureaus and scoring systems.
     */
    public RiskEvaluationResponse evaluateRisk(RiskEvaluationRequest request) {
        log.info("Evaluating risk for document: {}, amount: {}", 
                request.document(), request.requestedAmount());
        
        // Simulate processing delay
        try {
            Thread.sleep(random.nextInt(500) + 100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Generate a score based on document characteristics (simulation)
        int score = calculateScore(request.document(), request.requestedAmount());
        String riskLevel = determineRiskLevel(score);
        String detail = generateDetail(score, riskLevel);
        
        log.info("Risk evaluation complete - Score: {}, Level: {}", score, riskLevel);
        
        return new RiskEvaluationResponse(score, riskLevel, detail);
    }
    
    private int calculateScore(String document, BigDecimal amount) {
        // Simulate score calculation
        // Documents starting with "1" get lower scores (higher risk)
        // Documents starting with "9" get higher scores (lower risk)
        int baseScore = 500;
        
        if (document != null && !document.isEmpty()) {
            char firstDigit = document.charAt(0);
            if (Character.isDigit(firstDigit)) {
                int digit = Character.getNumericValue(firstDigit);
                baseScore = 400 + (digit * 50);
            }
        }
        
        // Amount factor: higher amounts slightly reduce score
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            if (amount.compareTo(new BigDecimal("10000000")) > 0) {
                baseScore -= 50;
            } else if (amount.compareTo(new BigDecimal("5000000")) > 0) {
                baseScore -= 25;
            }
        }
        
        // Add some randomness
        baseScore += random.nextInt(100) - 50;
        
        // Ensure score is within valid range
        return Math.max(300, Math.min(850, baseScore));
    }
    
    private String determineRiskLevel(int score) {
        if (score >= 700) {
            return "LOW";
        } else if (score >= 550) {
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