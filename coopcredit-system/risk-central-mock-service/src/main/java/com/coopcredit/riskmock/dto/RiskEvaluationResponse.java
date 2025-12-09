package com.coopcredit.riskmock.dto;

public record RiskEvaluationResponse(
        Integer score,
        String riskLevel,
        String detail
) {}