package com.coopcredit.creditapplication.infrastructure.controllers;

import com.coopcredit.creditapplication.domain.model.Member;
import com.coopcredit.creditapplication.domain.model.enums.MemberStatus;
import com.coopcredit.creditapplication.domain.ports.in.CreateMemberUseCase;
import com.coopcredit.creditapplication.domain.ports.in.GetMemberUseCase;
import com.coopcredit.creditapplication.domain.ports.in.UpdateMemberUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/members")
@Tag(name = "Members", description = "Member management")
@SecurityRequirement(name = "bearerAuth")
public class MemberController {
    
    private final CreateMemberUseCase createMemberUseCase;
    private final GetMemberUseCase getMemberUseCase;
    private final UpdateMemberUseCase updateMemberUseCase;
    
    public MemberController(CreateMemberUseCase createMemberUseCase,
                            GetMemberUseCase getMemberUseCase,
                            UpdateMemberUseCase updateMemberUseCase) {
        this.createMemberUseCase = createMemberUseCase;
        this.getMemberUseCase = getMemberUseCase;
        this.updateMemberUseCase = updateMemberUseCase;
    }
    
    @PostMapping
    @Operation(summary = "Create a new member")
    public ResponseEntity<MemberResponse> create(@Valid @RequestBody CreateMemberRequest request) {
        var command = new CreateMemberUseCase.CreateMemberCommand(
                request.document(),
                request.name(),
                request.salary(),
                request.affiliationDate() != null ? request.affiliationDate() : LocalDate.now()
        );
        Member member = createMemberUseCase.execute(command);
        return ResponseEntity.ok(toResponse(member));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get member by ID")
    public ResponseEntity<MemberResponse> getById(@PathVariable Long id) {
        Member member = getMemberUseCase.execute(id);
        return ResponseEntity.ok(toResponse(member));
    }
    
    @GetMapping("/document/{document}")
    @Operation(summary = "Get member by document")
    public ResponseEntity<MemberResponse> getByDocument(@PathVariable String document) {
        Member member = getMemberUseCase.executeByDocument(document);
        return ResponseEntity.ok(toResponse(member));
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update member")
    public ResponseEntity<MemberResponse> update(@PathVariable Long id, 
                                                  @Valid @RequestBody UpdateMemberRequest request) {
        var command = new UpdateMemberUseCase.UpdateMemberCommand(
                id, request.name(), request.salary(), request.status()
        );
        Member member = updateMemberUseCase.execute(command);
        return ResponseEntity.ok(toResponse(member));
    }
    
    private MemberResponse toResponse(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getDocument(),
                member.getName(),
                member.getSalary(),
                member.getAffiliationDate(),
                member.getStatus().name(),
                member.getSeniorityInMonths(),
                member.getMaxCreditAmount()
        );
    }
    
    // DTOs
    record CreateMemberRequest(
            @NotBlank String document,
            @NotBlank String name,
            @NotNull @Positive BigDecimal salary,
            LocalDate affiliationDate
    ) {}
    
    record UpdateMemberRequest(
            String name,
            @Positive BigDecimal salary,
            MemberStatus status
    ) {}
    
    record MemberResponse(
            Long id,
            String document,
            String name,
            BigDecimal salary,
            LocalDate affiliationDate,
            String status,
            long seniorityMonths,
            BigDecimal maxCreditAmount
    ) {}
}