package com.coopcredit.creditapplication.domain.services;

import com.coopcredit.creditapplication.domain.model.CreditApplication;
import com.coopcredit.creditapplication.domain.model.Member;
import com.coopcredit.creditapplication.domain.model.enums.ApplicationStatus;
import com.coopcredit.creditapplication.domain.model.enums.MemberStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class CreditEvaluationServiceTest {

    private CreditEvaluationService service;

    @BeforeEach
    void setUp() {
        service = new CreditEvaluationService();
    }

    @Test
    @DisplayName("Should approve when member meets all criteria")
    void shouldApproveWhenMemberMeetsAllCriteria() {
        Member member = createMember(
                BigDecimal.valueOf(5000),
                LocalDate.now().minusMonths(12)
        );
        CreditApplication application = createApplication(
                BigDecimal.valueOf(10000),
                12,
                BigDecimal.valueOf(1.5)
        );

        boolean meetsSeniority = service.checkSeniority(member);
        boolean meetsMaxAmount = service.checkMaxAmount(member, application);
        BigDecimal ratio = service.calculatePaymentToIncomeRatio(member, application);

        assertTrue(meetsSeniority);
        assertTrue(meetsMaxAmount);
        assertTrue(ratio.compareTo(BigDecimal.valueOf(0.30)) <= 0);
    }

    @Test
    @DisplayName("Should fail seniority check when member has less than 6 months")
    void shouldFailSeniorityCheck() {
        Member member = createMember(
                BigDecimal.valueOf(5000),
                LocalDate.now().minusMonths(3)
        );

        boolean meetsSeniority = service.checkSeniority(member);

        assertFalse(meetsSeniority);
    }

    @Test
    @DisplayName("Should fail max amount check when requested exceeds 4x salary")
    void shouldFailMaxAmountCheck() {
        Member member = createMember(
                BigDecimal.valueOf(5000),
                LocalDate.now().minusMonths(12)
        );
        CreditApplication application = createApplication(
                BigDecimal.valueOf(25000),
                12,
                BigDecimal.valueOf(1.5)
        );

        boolean meetsMaxAmount = service.checkMaxAmount(member, application);

        assertFalse(meetsMaxAmount);
    }

    @Test
    @DisplayName("Should calculate correct payment to income ratio")
    void shouldCalculatePaymentToIncomeRatio() {
        Member member = createMember(
                BigDecimal.valueOf(10000),
                LocalDate.now().minusMonths(12)
        );
        CreditApplication application = createApplication(
                BigDecimal.valueOf(12000),
                12,
                BigDecimal.valueOf(1.0)
        );

        BigDecimal ratio = service.calculatePaymentToIncomeRatio(member, application);

        assertNotNull(ratio);
        assertTrue(ratio.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("Should calculate member seniority in months")
    void shouldCalculateSeniorityMonths() {
        Member member = createMember(
                BigDecimal.valueOf(5000),
                LocalDate.now().minusMonths(18)
        );

        long seniority = service.calculateSeniorityMonths(member);

        assertEquals(18, seniority);
    }

    @Test
    @DisplayName("Should calculate max credit amount as 4x salary")
    void shouldCalculateMaxCreditAmount() {
        Member member = createMember(
                BigDecimal.valueOf(5000),
                LocalDate.now().minusMonths(12)
        );

        BigDecimal maxAmount = service.calculateMaxCreditAmount(member);

        assertEquals(BigDecimal.valueOf(20000), maxAmount);
    }

    private Member createMember(BigDecimal salary, LocalDate affiliationDate) {
        return Member.builder()
                .id(1L)
                .document("12345678")
                .name("Test Member")
                .salary(salary)
                .affiliationDate(affiliationDate)
                .status(MemberStatus.ACTIVE)
                .build();
    }

    private CreditApplication createApplication(BigDecimal amount, int termMonths, BigDecimal rate) {
        return CreditApplication.builder()
                .id(1L)
                .memberId(1L)
                .requestedAmount(amount)
                .termMonths(termMonths)
                .proposedRate(rate)
                .applicationDate(LocalDate.now())
                .status(ApplicationStatus.PENDING)
                .build();
    }
}
