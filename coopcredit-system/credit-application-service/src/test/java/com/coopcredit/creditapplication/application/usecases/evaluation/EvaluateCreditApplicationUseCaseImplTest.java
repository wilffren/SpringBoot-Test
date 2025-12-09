package com.coopcredit.creditapplication.application.usecases.evaluation;

import com.coopcredit.creditapplication.domain.model.CreditApplication;
import com.coopcredit.creditapplication.domain.model.Member;
import com.coopcredit.creditapplication.domain.model.RiskEvaluation;
import com.coopcredit.creditapplication.domain.model.enums.ApplicationStatus;
import com.coopcredit.creditapplication.domain.model.enums.MemberStatus;
import com.coopcredit.creditapplication.domain.model.enums.RiskLevel;
import com.coopcredit.creditapplication.domain.ports.out.CreditApplicationRepositoryPort;
import com.coopcredit.creditapplication.domain.ports.out.MemberRepositoryPort;
import com.coopcredit.creditapplication.domain.ports.out.RiskCentralPort;
import com.coopcredit.creditapplication.domain.ports.out.RiskEvaluationRepositoryPort;
import com.coopcredit.creditapplication.domain.services.CreditEvaluationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EvaluateCreditApplicationUseCase Unit Tests")
class EvaluateCreditApplicationUseCaseImplTest {

    @Mock
    private CreditApplicationRepositoryPort creditApplicationRepository;

    @Mock
    private MemberRepositoryPort memberRepository;

    @Mock
    private RiskEvaluationRepositoryPort riskEvaluationRepository;

    @Mock
    private RiskCentralPort riskCentralPort;

    @Spy
    private CreditEvaluationService creditEvaluationService = new CreditEvaluationService();

    @InjectMocks
    private EvaluateCreditApplicationUseCaseImpl evaluateUseCase;

    @Captor
    private ArgumentCaptor<RiskEvaluation> evaluationCaptor;

    @Captor
    private ArgumentCaptor<CreditApplication> applicationCaptor;

    private Member validMember;
    private CreditApplication pendingApplication;

    @BeforeEach
    void setUp() {
        validMember = Member.builder()
                .id(1L)
                .document("12345678")
                .name("John Doe")
                .salary(BigDecimal.valueOf(5000))
                .affiliationDate(LocalDate.now().minusMonths(12))
                .status(MemberStatus.ACTIVE)
                .build();

        pendingApplication = CreditApplication.builder()
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
    @DisplayName("When evaluating credit application")
    class EvaluationTests {

        @Test
        @DisplayName("Should approve application when all criteria met and low risk score")
        void shouldApproveWhenAllCriteriaMet() {
            // Given
            var riskResponse = new RiskCentralPort.RiskResponse(750, "LOW", "Good credit history");
            
            given(creditApplicationRepository.findById(1L)).willReturn(Optional.of(pendingApplication));
            given(memberRepository.findById(1L)).willReturn(Optional.of(validMember));
            given(riskCentralPort.evaluateRisk(any())).willReturn(riskResponse);
            given(riskEvaluationRepository.save(any())).willAnswer(inv -> {
                RiskEvaluation eval = inv.getArgument(0);
                return RiskEvaluation.builder()
                        .id(1L)
                        .creditApplicationId(eval.getCreditApplicationId())
                        .score(eval.getScore())
                        .riskLevel(eval.getRiskLevel())
                        .finalDecision(eval.getFinalDecision())
                        .build();
            });
            given(creditApplicationRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            // When
            var result = evaluateUseCase.execute(1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getFinalDecision()).isEqualTo("APPROVED");
            assertThat(result.getScore()).isEqualTo(750);
            assertThat(result.getRiskLevel()).isEqualTo(RiskLevel.LOW);

            then(riskEvaluationRepository).should().save(evaluationCaptor.capture());
            var savedEvaluation = evaluationCaptor.getValue();
            assertThat(savedEvaluation.isMeetsSeniority()).isTrue();
            assertThat(savedEvaluation.isMeetsMaxAmount()).isTrue();

            then(creditApplicationRepository).should().save(applicationCaptor.capture());
            var savedApplication = applicationCaptor.getValue();
            assertThat(savedApplication.getStatus()).isEqualTo(ApplicationStatus.APPROVED);
        }

        @Test
        @DisplayName("Should reject application when member lacks seniority")
        void shouldRejectWhenLacksSeniority() {
            // Given
            Member newMember = Member.builder()
                    .id(2L)
                    .document("87654321")
                    .name("New Member")
                    .salary(BigDecimal.valueOf(5000))
                    .affiliationDate(LocalDate.now().minusMonths(3))
                    .status(MemberStatus.ACTIVE)
                    .build();

            CreditApplication application = pendingApplication.toBuilder()
                    .memberId(2L)
                    .build();

            var riskResponse = new RiskCentralPort.RiskResponse(700, "MEDIUM", "Limited history");

            given(creditApplicationRepository.findById(1L)).willReturn(Optional.of(application));
            given(memberRepository.findById(2L)).willReturn(Optional.of(newMember));
            given(riskCentralPort.evaluateRisk(any())).willReturn(riskResponse);
            given(riskEvaluationRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
            given(creditApplicationRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            // When
            var result = evaluateUseCase.execute(1L);

            // Then
            assertThat(result.getFinalDecision()).isEqualTo("REJECTED");
            assertThat(result.isMeetsSeniority()).isFalse();
            assertThat(result.getReason()).contains("seniority");
        }

        @Test
        @DisplayName("Should reject application when amount exceeds maximum allowed")
        void shouldRejectWhenAmountExceedsMax() {
            // Given
            CreditApplication highAmountApp = pendingApplication.toBuilder()
                    .requestedAmount(BigDecimal.valueOf(50000)) // 10x salary
                    .build();

            var riskResponse = new RiskCentralPort.RiskResponse(800, "LOW", "Excellent");

            given(creditApplicationRepository.findById(1L)).willReturn(Optional.of(highAmountApp));
            given(memberRepository.findById(1L)).willReturn(Optional.of(validMember));
            given(riskCentralPort.evaluateRisk(any())).willReturn(riskResponse);
            given(riskEvaluationRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
            given(creditApplicationRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            // When
            var result = evaluateUseCase.execute(1L);

            // Then
            assertThat(result.getFinalDecision()).isEqualTo("REJECTED");
            assertThat(result.isMeetsMaxAmount()).isFalse();
            assertThat(result.getReason()).contains("maximum");
        }

        @Test
        @DisplayName("Should reject application when risk score is too low")
        void shouldRejectWhenHighRiskScore() {
            // Given
            var riskResponse = new RiskCentralPort.RiskResponse(300, "HIGH", "Bad credit history");

            given(creditApplicationRepository.findById(1L)).willReturn(Optional.of(pendingApplication));
            given(memberRepository.findById(1L)).willReturn(Optional.of(validMember));
            given(riskCentralPort.evaluateRisk(any())).willReturn(riskResponse);
            given(riskEvaluationRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
            given(creditApplicationRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            // When
            var result = evaluateUseCase.execute(1L);

            // Then
            assertThat(result.getFinalDecision()).isEqualTo("REJECTED");
            assertThat(result.getRiskLevel()).isEqualTo(RiskLevel.HIGH);
        }
    }

    @Nested
    @DisplayName("When handling errors")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should throw exception when application not found")
        void shouldThrowWhenApplicationNotFound() {
            // Given
            given(creditApplicationRepository.findById(999L)).willReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> evaluateUseCase.execute(999L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("not found");

            then(memberRepository).shouldHaveNoInteractions();
            then(riskCentralPort).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("Should throw exception when member not found")
        void shouldThrowWhenMemberNotFound() {
            // Given
            given(creditApplicationRepository.findById(1L)).willReturn(Optional.of(pendingApplication));
            given(memberRepository.findById(1L)).willReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> evaluateUseCase.execute(1L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Member");

            then(riskCentralPort).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("Should throw exception when application already evaluated")
        void shouldThrowWhenAlreadyEvaluated() {
            // Given
            CreditApplication approvedApp = pendingApplication.toBuilder()
                    .status(ApplicationStatus.APPROVED)
                    .build();

            given(creditApplicationRepository.findById(1L)).willReturn(Optional.of(approvedApp));

            // When/Then
            assertThatThrownBy(() -> evaluateUseCase.execute(1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("already");
        }

        @Test
        @DisplayName("Should handle risk central service failure gracefully")
        void shouldHandleRiskCentralFailure() {
            // Given
            given(creditApplicationRepository.findById(1L)).willReturn(Optional.of(pendingApplication));
            given(memberRepository.findById(1L)).willReturn(Optional.of(validMember));
            given(riskCentralPort.evaluateRisk(any())).willThrow(new RuntimeException("Service unavailable"));

            // When/Then
            assertThatThrownBy(() -> evaluateUseCase.execute(1L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("unavailable");
        }
    }

    @Nested
    @DisplayName("Payment to income ratio calculations")
    class PaymentRatioTests {

        @ParameterizedTest
        @MethodSource("providePaymentRatioScenarios")
        @DisplayName("Should calculate correct payment to income ratio")
        void shouldCalculatePaymentRatio(BigDecimal salary, BigDecimal amount, int term, boolean shouldPass) {
            // Given
            Member member = validMember.toBuilder().salary(salary).build();
            CreditApplication app = pendingApplication.toBuilder()
                    .requestedAmount(amount)
                    .termMonths(term)
                    .build();

            var riskResponse = new RiskCentralPort.RiskResponse(700, "MEDIUM", "OK");

            given(creditApplicationRepository.findById(1L)).willReturn(Optional.of(app));
            given(memberRepository.findById(1L)).willReturn(Optional.of(member));
            given(riskCentralPort.evaluateRisk(any())).willReturn(riskResponse);
            given(riskEvaluationRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
            given(creditApplicationRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            // When
            var result = evaluateUseCase.execute(1L);

            // Then
            if (shouldPass) {
                assertThat(result.getPaymentToIncomeRatio()).isLessThanOrEqualTo(BigDecimal.valueOf(0.30));
            } else {
                assertThat(result.getPaymentToIncomeRatio()).isGreaterThan(BigDecimal.valueOf(0.30));
            }
        }

        static Stream<Arguments> providePaymentRatioScenarios() {
            return Stream.of(
                    Arguments.of(BigDecimal.valueOf(10000), BigDecimal.valueOf(12000), 12, true),  // 10% ratio
                    Arguments.of(BigDecimal.valueOf(5000), BigDecimal.valueOf(12000), 12, true),   // 20% ratio
                    Arguments.of(BigDecimal.valueOf(3000), BigDecimal.valueOf(12000), 12, false),  // 33% ratio
                    Arguments.of(BigDecimal.valueOf(2000), BigDecimal.valueOf(12000), 12, false)   // 50% ratio
            );
        }
    }
}
