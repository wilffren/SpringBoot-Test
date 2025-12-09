package com.coopcredit.creditapplication.domain.services;

import com.coopcredit.creditapplication.domain.model.CreditApplication;
import com.coopcredit.creditapplication.domain.model.Member;
import com.coopcredit.creditapplication.domain.model.RiskEvaluation;
import com.coopcredit.creditapplication.domain.model.enums.ApplicationStatus;
import com.coopcredit.creditapplication.domain.model.enums.MemberStatus;
import com.coopcredit.creditapplication.domain.model.enums.RiskLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreditEvaluationService Unit Tests")
class CreditEvaluationServiceTest {

    @Spy
    private CreditEvaluationService service;

    private Member validMember;
    private CreditApplication validApplication;

    @BeforeEach
    void setUp() {
        validMember = Member.builder()
                .id(1L)
                .document("12345678")
                .name("Test Member")
                .salary(BigDecimal.valueOf(5000))
                .affiliationDate(LocalDate.now().minusMonths(12))
                .status(MemberStatus.ACTIVE)
                .build();

        validApplication = CreditApplication.builder()
                .id(1L)
                .memberId(1L)
                .requestedAmount(BigDecimal.valueOf(10000))
                .termMonths(12)
                .proposedRate(BigDecimal.valueOf(1.5))
                .applicationDate(LocalDate.now())
                .status(ApplicationStatus.PENDING)
                .build();
    }

    @Nested
    @DisplayName("Seniority validation")
    class SeniorityTests {

        @ParameterizedTest
        @ValueSource(ints = {6, 7, 12, 24, 60})
        @DisplayName("Should pass seniority check when member has 6+ months")
        void shouldPassSeniorityWhenSufficientMonths(int months) {
            // Given
            Member member = validMember.toBuilder()
                    .affiliationDate(LocalDate.now().minusMonths(months))
                    .build();

            // When
            boolean result = service.checkSeniority(member);

            // Then
            assertThat(result).isTrue();
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 2, 3, 4, 5})
        @DisplayName("Should fail seniority check when member has less than 6 months")
        void shouldFailSeniorityWhenInsufficientMonths(int months) {
            // Given
            Member member = validMember.toBuilder()
                    .affiliationDate(LocalDate.now().minusMonths(months))
                    .build();

            // When
            boolean result = service.checkSeniority(member);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should calculate exact seniority in months")
        void shouldCalculateExactSeniority() {
            // Given
            Member member = validMember.toBuilder()
                    .affiliationDate(LocalDate.now().minusMonths(18).minusDays(15))
                    .build();

            // When
            long seniority = service.calculateSeniorityMonths(member);

            // Then
            assertThat(seniority).isGreaterThanOrEqualTo(18);
        }

        @Test
        @DisplayName("Should handle member affiliated today")
        void shouldHandleMemberAffiliatedToday() {
            // Given
            Member member = validMember.toBuilder()
                    .affiliationDate(LocalDate.now())
                    .build();

            // When
            long seniority = service.calculateSeniorityMonths(member);

            // Then
            assertThat(seniority).isZero();
        }
    }

    @Nested
    @DisplayName("Maximum amount validation")
    class MaxAmountTests {

        @ParameterizedTest
        @CsvSource({
                "5000, 20000, true",   // Exactly 4x salary
                "5000, 19999, true",   // Just under 4x
                "5000, 10000, true",   // 2x salary
                "5000, 20001, false",  // Just over 4x
                "5000, 25000, false",  // 5x salary
                "10000, 40000, true",  // 4x of higher salary
                "3000, 15000, false"   // 5x of lower salary
        })
        @DisplayName("Should validate max amount based on 4x salary rule")
        void shouldValidateMaxAmount(double salary, double requestedAmount, boolean expected) {
            // Given
            Member member = validMember.toBuilder()
                    .salary(BigDecimal.valueOf(salary))
                    .build();
            CreditApplication application = validApplication.toBuilder()
                    .requestedAmount(BigDecimal.valueOf(requestedAmount))
                    .build();

            // When
            boolean result = service.checkMaxAmount(member, application);

            // Then
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("Should calculate max credit amount as 4x salary")
        void shouldCalculateMaxCreditAmount() {
            // Given
            BigDecimal salary = BigDecimal.valueOf(7500);
            Member member = validMember.toBuilder().salary(salary).build();

            // When
            BigDecimal maxAmount = service.calculateMaxCreditAmount(member);

            // Then
            assertThat(maxAmount).isEqualByComparingTo(BigDecimal.valueOf(30000));
        }
    }

    @Nested
    @DisplayName("Payment to income ratio calculation")
    class PaymentRatioTests {

        @ParameterizedTest
        @MethodSource("providePaymentRatioScenarios")
        @DisplayName("Should calculate payment to income ratio correctly")
        void shouldCalculatePaymentRatio(double salary, double amount, int term, double rate, double expectedRatio) {
            // Given
            Member member = validMember.toBuilder()
                    .salary(BigDecimal.valueOf(salary))
                    .build();
            CreditApplication application = validApplication.toBuilder()
                    .requestedAmount(BigDecimal.valueOf(amount))
                    .termMonths(term)
                    .proposedRate(BigDecimal.valueOf(rate))
                    .build();

            // When
            BigDecimal ratio = service.calculatePaymentToIncomeRatio(member, application);

            // Then
            assertThat(ratio.setScale(2, RoundingMode.HALF_UP))
                    .isCloseTo(BigDecimal.valueOf(expectedRatio), within(BigDecimal.valueOf(0.02)));
        }

        static Stream<Arguments> providePaymentRatioScenarios() {
            return Stream.of(
                    Arguments.of(10000, 12000, 12, 0, 0.10),   // 1000/10000 = 10%
                    Arguments.of(5000, 12000, 12, 0, 0.20),    // 1000/5000 = 20%
                    Arguments.of(3000, 12000, 12, 0, 0.33),    // 1000/3000 = 33%
                    Arguments.of(10000, 36000, 12, 0, 0.30),   // 3000/10000 = 30%
                    Arguments.of(5000, 6000, 12, 0, 0.10)      // 500/5000 = 10%
            );
        }

        @Test
        @DisplayName("Should reject when payment exceeds 30% of income")
        void shouldRejectWhenPaymentExceeds30Percent() {
            // Given
            Member member = validMember.toBuilder()
                    .salary(BigDecimal.valueOf(2000))
                    .build();
            CreditApplication application = validApplication.toBuilder()
                    .requestedAmount(BigDecimal.valueOf(12000))
                    .termMonths(12)
                    .build();

            // When
            BigDecimal ratio = service.calculatePaymentToIncomeRatio(member, application);
            boolean meetsRatio = ratio.compareTo(BigDecimal.valueOf(0.30)) <= 0;

            // Then
            assertThat(meetsRatio).isFalse();
        }

        @Test
        @DisplayName("Should approve when payment is exactly 30% of income")
        void shouldApproveWhenPaymentExactly30Percent() {
            // Given
            Member member = validMember.toBuilder()
                    .salary(BigDecimal.valueOf(10000))
                    .build();
            CreditApplication application = validApplication.toBuilder()
                    .requestedAmount(BigDecimal.valueOf(36000))
                    .termMonths(12)
                    .proposedRate(BigDecimal.ZERO)
                    .build();

            // When
            BigDecimal ratio = service.calculatePaymentToIncomeRatio(member, application);

            // Then
            assertThat(ratio).isEqualByComparingTo(BigDecimal.valueOf(0.30));
        }
    }

    @Nested
    @DisplayName("Risk level classification")
    class RiskLevelTests {

        @ParameterizedTest
        @CsvSource({
                "800, LOW",
                "750, LOW",
                "700, LOW",
                "699, MEDIUM",
                "600, MEDIUM",
                "500, MEDIUM",
                "499, HIGH",
                "300, HIGH",
                "0, HIGH"
        })
        @DisplayName("Should classify risk level based on score")
        void shouldClassifyRiskLevel(int score, RiskLevel expectedLevel) {
            // When
            RiskLevel result = service.classifyRiskLevel(score);

            // Then
            assertThat(result).isEqualTo(expectedLevel);
        }
    }

    @Nested
    @DisplayName("Complete evaluation process")
    class CompleteEvaluationTests {

        @Test
        @DisplayName("Should build complete evaluation with all criteria passing")
        void shouldBuildCompleteEvaluationApproved() {
            // Given
            int riskScore = 750;
            String riskDetail = "Good credit history";

            // When
            RiskEvaluation evaluation = service.buildEvaluation(
                    validApplication.getId(),
                    validMember,
                    validApplication,
                    riskScore,
                    riskDetail
            );

            // Then
            assertThat(evaluation).isNotNull();
            assertThat(evaluation.getCreditApplicationId()).isEqualTo(1L);
            assertThat(evaluation.getScore()).isEqualTo(750);
            assertThat(evaluation.getRiskLevel()).isEqualTo(RiskLevel.LOW);
            assertThat(evaluation.isMeetsSeniority()).isTrue();
            assertThat(evaluation.isMeetsMaxAmount()).isTrue();
            assertThat(evaluation.getFinalDecision()).isEqualTo("APPROVED");

            then(service).should().checkSeniority(validMember);
            then(service).should().checkMaxAmount(validMember, validApplication);
            then(service).should().calculatePaymentToIncomeRatio(validMember, validApplication);
        }

        @Test
        @DisplayName("Should build evaluation with rejection due to seniority")
        void shouldBuildEvaluationRejectedSeniority() {
            // Given
            Member newMember = validMember.toBuilder()
                    .affiliationDate(LocalDate.now().minusMonths(3))
                    .build();

            // When
            RiskEvaluation evaluation = service.buildEvaluation(
                    validApplication.getId(),
                    newMember,
                    validApplication,
                    800,
                    "Excellent"
            );

            // Then
            assertThat(evaluation.isMeetsSeniority()).isFalse();
            assertThat(evaluation.getFinalDecision()).isEqualTo("REJECTED");
            assertThat(evaluation.getReason()).contains("seniority");
        }

        @Test
        @DisplayName("Should build evaluation with rejection due to high risk")
        void shouldBuildEvaluationRejectedHighRisk() {
            // Given - low score indicates high risk
            int lowScore = 350;

            // When
            RiskEvaluation evaluation = service.buildEvaluation(
                    validApplication.getId(),
                    validMember,
                    validApplication,
                    lowScore,
                    "Bad credit history"
            );

            // Then
            assertThat(evaluation.getRiskLevel()).isEqualTo(RiskLevel.HIGH);
            assertThat(evaluation.getFinalDecision()).isEqualTo("REJECTED");
            assertThat(evaluation.getReason()).contains("risk");
        }

        @Test
        @DisplayName("Should include all rejection reasons when multiple criteria fail")
        void shouldIncludeAllRejectionReasons() {
            // Given
            Member newMember = validMember.toBuilder()
                    .affiliationDate(LocalDate.now().minusMonths(2))
                    .salary(BigDecimal.valueOf(2000))
                    .build();
            CreditApplication bigLoan = validApplication.toBuilder()
                    .requestedAmount(BigDecimal.valueOf(50000))
                    .build();

            // When
            RiskEvaluation evaluation = service.buildEvaluation(
                    bigLoan.getId(),
                    newMember,
                    bigLoan,
                    300,
                    "Very bad"
            );

            // Then
            assertThat(evaluation.getFinalDecision()).isEqualTo("REJECTED");
            assertThat(evaluation.isMeetsSeniority()).isFalse();
            assertThat(evaluation.isMeetsMaxAmount()).isFalse();
            assertThat(evaluation.getRiskLevel()).isEqualTo(RiskLevel.HIGH);
        }
    }
}
