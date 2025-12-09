package com.coopcredit.creditapplication.infrastructure.adapters.http;

import com.coopcredit.creditapplication.domain.model.enums.RiskLevel;
import com.coopcredit.creditapplication.domain.ports.out.RiskCentralPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;

@Component
public class RiskCentralHttpAdapter implements RiskCentralPort {
    
    private static final Logger log = LoggerFactory.getLogger(RiskCentralHttpAdapter.class);
    
    private final WebClient webClient;
    private final String riskCentralBaseUrl;
    
    public RiskCentralHttpAdapter(
            WebClient.Builder webClientBuilder,
            @Value("${risk-central.base-url:http://localhost:8081}") String riskCentralBaseUrl) {
        this.riskCentralBaseUrl = riskCentralBaseUrl;
        this.webClient = webClientBuilder.baseUrl(riskCentralBaseUrl).build();
    }
    
    @Override
    @CircuitBreaker(name = "riskCentral", fallbackMethod = "fallbackEvaluateRisk")
    @Retry(name = "riskCentral")
    public RiskCentralResponse evaluateRisk(RiskCentralRequest request) {
        log.info("Calling Risk Central service for document: {}", request.document());
        
        RiskCentralApiResponse response = webClient.post()
                .uri("/api/risk/evaluate")
                .bodyValue(new RiskCentralApiRequest(request.document(), request.requestedAmount()))
                .retrieve()
                .bodyToMono(RiskCentralApiResponse.class)
                .block();
        
        if (response == null) {
            return fallbackEvaluateRisk(request, new RuntimeException("Null response"));
        }
        
        return new RiskCentralResponse(
                response.score(),
                mapRiskLevel(response.riskLevel()),
                response.detail()
        );
    }
    
    private RiskCentralResponse fallbackEvaluateRisk(RiskCentralRequest request, Throwable t) {
        log.warn("Risk Central service unavailable, using fallback. Error: {}", t.getMessage());
        // Default to MEDIUM risk when service is unavailable
        return new RiskCentralResponse(
                600,
                RiskLevel.MEDIUM,
                "Risk Central service unavailable - using default evaluation"
        );
    }
    
    private RiskLevel mapRiskLevel(String level) {
        return switch (level.toUpperCase()) {
            case "LOW" -> RiskLevel.LOW;
            case "HIGH" -> RiskLevel.HIGH;
            default -> RiskLevel.MEDIUM;
        };
    }
    
    // DTOs for API communication
    record RiskCentralApiRequest(String document, BigDecimal requestedAmount) {}
    record RiskCentralApiResponse(Integer score, String riskLevel, String detail) {}
}