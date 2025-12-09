package com.coopcredit.riskmock.dto;

import java.math.BigDecimal;

public record RiskEvaluationRequest(
        String document,
        BigDecimal requestedAmount
) {}