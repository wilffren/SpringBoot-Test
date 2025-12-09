package com.coopcredit.riskmock.controller;

import com.coopcredit.riskmock.dto.RiskEvaluationRequest;
import com.coopcredit.riskmock.dto.RiskEvaluationResponse;
import com.coopcredit.riskmock.service.RiskScoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/risk")
@Tag(name = "Risk Evaluation", description = "External risk central service mock")
public class RiskEvaluationController {
    
    private final RiskScoreService riskScoreService;
    
    public RiskEvaluationController(RiskScoreService riskScoreService) {
        this.riskScoreService = riskScoreService;
    }
    
    @PostMapping("/evaluate")
    @Operation(summary = "Evaluate credit risk for a document and amount")
    public ResponseEntity<RiskEvaluationResponse> evaluateRisk(@RequestBody RiskEvaluationRequest request) {
        RiskEvaluationResponse response = riskScoreService.evaluateRisk(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/health")
    @Operation(summary = "Health check endpoint")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Risk Central Mock Service is running");
    }
}