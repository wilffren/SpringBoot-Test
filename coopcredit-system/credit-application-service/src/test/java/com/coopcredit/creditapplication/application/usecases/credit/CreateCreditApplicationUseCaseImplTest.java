package com.coopcredit.creditapplication.application.usecases.credit;

import com.coopcredit.creditapplication.domain.model.CreditApplication;
import com.coopcredit.creditapplication.domain.model.Member;
import com.coopcredit.creditapplication.domain.model.enums.ApplicationStatus;
import com.coopcredit.creditapplication.domain.model.enums.MemberStatus;
import com.coopcredit.creditapplication.domain.ports.in.CreateCreditApplicationUseCase;
import com.coopcredit.creditapplication.domain.ports.out.CreditApplicationRepositoryPort;
import com.coopcredit.creditapplication.domain.ports.out.MemberRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateCreditApplicationUseCase Unit Tests")
class CreateCreditApplicationUseCaseImplTest {

    @Mock
    private CreditApplicationRepositoryPort creditApplicationRepository;

    @Mock
    private MemberRepositoryPort memberRepository;

    @InjectMocks
    private CreateCreditApplicationUseCaseImpl createCreditApplicationUseCase;

    @Captor
    private ArgumentCaptor<CreditApplication> applicationCaptor;

    private Member activeMember;
    private CreateCreditApplicationUseCase.CreateApplicationCommand validCommand;

    @BeforeEach
    void setUp() {
        activeMember = Member.builder()
                .id(1L)
                .document("12345678")
                .name("John Doe")
                .salary(BigDecimal.valueOf(5000))
                .affiliationDate(LocalDate.now().minusMonths(12))
                .status(MemberStatus.ACTIVE)
                .build();

        validCommand = new CreateCreditApplicationUseCase.CreateApplicationCommand(
                1L,
                BigDecimal.valueOf(10000),
                12,
                BigDecimal.valueOf(1.5)
        );
    }

    @Nested
    @DisplayName("When creating a new credit application")
    class CreateApplicationTests {

        @Test
        @DisplayName("Should create application with PENDING status")
        void shouldCreateApplicationWithPendingStatus() {
            // Given
            given(memberRepository.findById(1L)).willReturn(Optional.of(activeMember));
            given(creditApplicationRepository.findByMemberId(1L)).willReturn(List.of());
            given(creditApplicationRepository.save(any(CreditApplication.class))).willAnswer(inv -> {
                CreditApplication app = inv.getArgument(0);
                return app.toBuilder().id(1L).build();
            });

            // When
            CreditApplication result = createCreditApplicationUseCase.execute(validCommand);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(ApplicationStatus.PENDING);
            assertThat(result.getApplicationDate()).isEqualTo(LocalDate.now());

            then(creditApplicationRepository).should().save(applicationCaptor.capture());
            CreditApplication saved = applicationCaptor.getValue();
            assertThat(saved.getMemberId()).isEqualTo(1L);
            assertThat(saved.getRequestedAmount()).isEqualByComparingTo(BigDecimal.valueOf(10000));
        }

        @Test
        @DisplayName("Should set application date to current date")
        void shouldSetApplicationDateToCurrentDate() {
            // Given
            given(memberRepository.findById(1L)).willReturn(Optional.of(activeMember));
            given(creditApplicationRepository.findByMemberId(1L)).willReturn(List.of());
            given(creditApplicationRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            // When
            CreditApplication result = createCreditApplicationUseCase.execute(validCommand);

            // Then
            assertThat(result.getApplicationDate()).isEqualTo(LocalDate.now());
        }

        @Test
        @DisplayName("Should allow multiple applications for same member if none pending")
        void shouldAllowMultipleApplicationsIfNonePending() {
            // Given
            CreditApplication previousApproved = CreditApplication.builder()
                    .id(1L)
                    .memberId(1L)
                    .status(ApplicationStatus.APPROVED)
                    .build();

            given(memberRepository.findById(1L)).willReturn(Optional.of(activeMember));
            given(creditApplicationRepository.findByMemberId(1L)).willReturn(List.of(previousApproved));
            given(creditApplicationRepository.save(any())).willAnswer(inv -> {
                CreditApplication app = inv.getArgument(0);
                return app.toBuilder().id(2L).build();
            });

            // When
            CreditApplication result = createCreditApplicationUseCase.execute(validCommand);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(2L);
        }
    }

    @Nested
    @DisplayName("When validating member")
    class MemberValidationTests {

        @Test
        @DisplayName("Should throw exception when member not found")
        void shouldThrowWhenMemberNotFound() {
            // Given
            given(memberRepository.findById(999L)).willReturn(Optional.empty());

            CreateCreditApplicationUseCase.CreateApplicationCommand command =
                    new CreateCreditApplicationUseCase.CreateApplicationCommand(
                            999L, BigDecimal.valueOf(5000), 12, BigDecimal.valueOf(1.0)
                    );

            // When/Then
            assertThatThrownBy(() -> createCreditApplicationUseCase.execute(command))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Member not found");

            then(creditApplicationRepository).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("Should throw exception when member is inactive")
        void shouldThrowWhenMemberInactive() {
            // Given
            Member inactiveMember = activeMember.toBuilder()
                    .status(MemberStatus.INACTIVE)
                    .build();

            given(memberRepository.findById(1L)).willReturn(Optional.of(inactiveMember));

            // When/Then
            assertThatThrownBy(() -> createCreditApplicationUseCase.execute(validCommand))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("inactive");

            then(creditApplicationRepository).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("Should throw exception when member has pending application")
        void shouldThrowWhenMemberHasPendingApplication() {
            // Given
            CreditApplication pendingApp = CreditApplication.builder()
                    .id(1L)
                    .memberId(1L)
                    .status(ApplicationStatus.PENDING)
                    .build();

            given(memberRepository.findById(1L)).willReturn(Optional.of(activeMember));
            given(creditApplicationRepository.findByMemberId(1L)).willReturn(List.of(pendingApp));

            // When/Then
            assertThatThrownBy(() -> createCreditApplicationUseCase.execute(validCommand))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("pending");
        }
    }

    @Nested
    @DisplayName("When validating application data")
    class ApplicationValidationTests {

        @ParameterizedTest
        @ValueSource(doubles = {-1000, -1, 0})
        @DisplayName("Should throw exception when requested amount is not positive")
        void shouldThrowWhenAmountNotPositive(double amount) {
            // Given
            CreateCreditApplicationUseCase.CreateApplicationCommand command =
                    new CreateCreditApplicationUseCase.CreateApplicationCommand(
                            1L, BigDecimal.valueOf(amount), 12, BigDecimal.valueOf(1.0)
                    );

            given(memberRepository.findById(1L)).willReturn(Optional.of(activeMember));

            // When/Then
            assertThatThrownBy(() -> createCreditApplicationUseCase.execute(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("amount");
        }

        @ParameterizedTest
        @ValueSource(ints = {-12, -1, 0})
        @DisplayName("Should throw exception when term months is not positive")
        void shouldThrowWhenTermNotPositive(int term) {
            // Given
            CreateCreditApplicationUseCase.CreateApplicationCommand command =
                    new CreateCreditApplicationUseCase.CreateApplicationCommand(
                            1L, BigDecimal.valueOf(10000), term, BigDecimal.valueOf(1.0)
                    );

            given(memberRepository.findById(1L)).willReturn(Optional.of(activeMember));

            // When/Then
            assertThatThrownBy(() -> createCreditApplicationUseCase.execute(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("term");
        }

        @ParameterizedTest
        @ValueSource(ints = {61, 100, 120})
        @DisplayName("Should throw exception when term exceeds maximum allowed")
        void shouldThrowWhenTermExceedsMax(int term) {
            // Given
            CreateCreditApplicationUseCase.CreateApplicationCommand command =
                    new CreateCreditApplicationUseCase.CreateApplicationCommand(
                            1L, BigDecimal.valueOf(10000), term, BigDecimal.valueOf(1.0)
                    );

            given(memberRepository.findById(1L)).willReturn(Optional.of(activeMember));

            // When/Then
            assertThatThrownBy(() -> createCreditApplicationUseCase.execute(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("60 months");
        }

        @ParameterizedTest
        @ValueSource(doubles = {-1.0, 0, 50.1, 100})
        @DisplayName("Should throw exception when rate is out of valid range")
        void shouldThrowWhenRateOutOfRange(double rate) {
            // Given
            CreateCreditApplicationUseCase.CreateApplicationCommand command =
                    new CreateCreditApplicationUseCase.CreateApplicationCommand(
                            1L, BigDecimal.valueOf(10000), 12, BigDecimal.valueOf(rate)
                    );

            given(memberRepository.findById(1L)).willReturn(Optional.of(activeMember));

            // When/Then
            assertThatThrownBy(() -> createCreditApplicationUseCase.execute(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("rate");
        }
    }

    @Nested
    @DisplayName("When calculating monthly payment")
    class PaymentCalculationTests {

        @ParameterizedTest
        @MethodSource("providePaymentCalculationScenarios")
        @DisplayName("Should calculate correct monthly payment")
        void shouldCalculateMonthlyPayment(BigDecimal amount, int term, BigDecimal rate, BigDecimal expectedPayment) {
            // Given
            CreateCreditApplicationUseCase.CreateApplicationCommand command =
                    new CreateCreditApplicationUseCase.CreateApplicationCommand(1L, amount, term, rate);

            given(memberRepository.findById(1L)).willReturn(Optional.of(activeMember));
            given(creditApplicationRepository.findByMemberId(1L)).willReturn(List.of());
            given(creditApplicationRepository.save(any())).willAnswer(inv -> {
                CreditApplication app = inv.getArgument(0);
                return app.toBuilder().id(1L).build();
            });

            // When
            CreditApplication result = createCreditApplicationUseCase.execute(command);

            // Then
            BigDecimal monthlyPayment = result.calculateMonthlyPayment();
            assertThat(monthlyPayment).isCloseTo(expectedPayment, within(BigDecimal.valueOf(10)));
        }

        static Stream<Arguments> providePaymentCalculationScenarios() {
            return Stream.of(
                    Arguments.of(BigDecimal.valueOf(12000), 12, BigDecimal.valueOf(0), BigDecimal.valueOf(1000)),
                    Arguments.of(BigDecimal.valueOf(24000), 24, BigDecimal.valueOf(0), BigDecimal.valueOf(1000)),
                    Arguments.of(BigDecimal.valueOf(12000), 12, BigDecimal.valueOf(12), BigDecimal.valueOf(1066))
            );
        }
    }
}
