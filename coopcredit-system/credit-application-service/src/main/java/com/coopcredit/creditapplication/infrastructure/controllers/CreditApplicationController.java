package com.coopcredit.creditapplication.infrastructure.controllers;

import com.coopcredit.creditapplication.domain.model.CreditApplication;
import com.coopcredit.creditapplication.domain.model.RiskEvaluation;
import com.coopcredit.creditapplication.domain.model.enums.ApplicationStatus;
import com.coopcredit.creditapplication.domain.ports.in.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/credit-applications")
@Tag(name = "3. Credit Applications", description = "Credit application management - Requires JWT token")
@SecurityRequirement(name = "bearerAuth")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid", content = @Content),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions", content = @Content)
})
public class CreditApplicationController {
    
    private final CreateCreditApplicationUseCase createCreditApplicationUseCase;
    private final GetCreditApplicationUseCase getCreditApplicationUseCase;
    private final ListCreditApplicationsUseCase listCreditApplicationsUseCase;
    private final EvaluateCreditApplicationUseCase evaluateCreditApplicationUseCase;
    
    public CreditApplicationController(
            CreateCreditApplicationUseCase createCreditApplicationUseCase,
            GetCreditApplicationUseCase getCreditApplicationUseCase,
            ListCreditApplicationsUseCase listCreditApplicationsUseCase,
            EvaluateCreditApplicationUseCase evaluateCreditApplicationUseCase) {
        this.createCreditApplicationUseCase = createCreditApplicationUseCase;
        this.getCreditApplicationUseCase = getCreditApplicationUseCase;
        this.listCreditApplicationsUseCase = listCreditApplicationsUseCase;
        this.evaluateCreditApplicationUseCase = evaluateCreditApplicationUseCase;
    }
    
    @PostMapping
    @Operation(summary = "Create a new credit application")
    public ResponseEntity<CreditApplicationResponse> create(@Valid @RequestBody CreateApplicationRequest request) {
        var command = new CreateCreditApplicationUseCase.CreateCreditApplicationCommand(
                request.memberId(),
                request.requestedAmount(),
                request.termMonths(),
                request.proposedRate()
        );
        CreditApplication application = createCreditApplicationUseCase.execute(command);
        return ResponseEntity.ok(toResponse(application));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get credit application by ID")
    public ResponseEntity<CreditApplicationResponse> getById(@PathVariable Long id) {
        CreditApplication application = getCreditApplicationUseCase.execute(id);
        return ResponseEntity.ok(toResponse(application));
    }
    
    @GetMapping
    @Operation(summary = "List all credit applications")
    public ResponseEntity<List<CreditApplicationResponse>> list(
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) ApplicationStatus status) {
        List<CreditApplication> applications;
        if (memberId != null) {
            applications = listCreditApplicationsUseCase.executeByMemberId(memberId);
        } else if (status != null) {
            applications = listCreditApplicationsUseCase.executeByStatus(status);
        } else {
            applications = listCreditApplicationsUseCase.execute();
        }
        return ResponseEntity.ok(applications.stream().map(this::toResponse).toList());
    }
    
    @PostMapping("/{id}/evaluate")
    @Operation(summary = "Evaluate a credit application")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    public ResponseEntity<EvaluationResponse> evaluate(@PathVariable Long id) {
        RiskEvaluation evaluation = evaluateCreditApplicationUseCase.execute(id);
        return ResponseEntity.ok(toEvaluationResponse(evaluation));
    }
    
    private CreditApplicationResponse toResponse(CreditApplication app) {
        return new CreditApplicationResponse(
                app.getId(),
                app.getMemberId(),
                app.getRequestedAmount(),
                app.getTermMonths(),
                app.getProposedRate(),
                app.getApplicationDate(),
                app.getStatus().name()
        );
    }
    
    private EvaluationResponse toEvaluationResponse(RiskEvaluation eval) {
        return new EvaluationResponse(
                eval.getId(),
                eval.getCreditApplicationId(),
                eval.getScore(),
                eval.getRiskLevel().name(),
                eval.getPaymentToIncomeRatio(),
                eval.isMeetsSeniority(),
                eval.isMeetsMaxAmount(),
                eval.getFinalDecision().name(),
                eval.getReason()
        );
    }
    
    // DTOs
    record CreateApplicationRequest(
            @NotNull Long memberId,
            @NotNull @Positive BigDecimal requestedAmount,
            @NotNull @Positive Integer termMonths,
            @NotNull @Positive BigDecimal proposedRate
    ) {}
    
    record CreditApplicationResponse(
            Long id,
            Long memberId,
            BigDecimal requestedAmount,
            Integer termMonths,
            BigDecimal proposedRate,
            LocalDate applicationDate,
            String status
    ) {}
    
    record EvaluationResponse(
            Long id,
            Long creditApplicationId,
            Integer score,
            String riskLevel,
            BigDecimal paymentToIncomeRatio,
            boolean meetsSeniority,
            boolean meetsMaxAmount,
            String finalDecision,
            String reason
    ) {}
}