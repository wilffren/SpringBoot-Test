package com.coopcredit.creditapplication.application.usecases.member;

import com.coopcredit.creditapplication.domain.model.Member;
import com.coopcredit.creditapplication.domain.model.enums.MemberStatus;
import com.coopcredit.creditapplication.domain.ports.in.CreateMemberUseCase;
import com.coopcredit.creditapplication.domain.ports.out.MemberRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateMemberUseCase Unit Tests")
class CreateMemberUseCaseImplTest {

    @Mock
    private MemberRepositoryPort memberRepository;

    @InjectMocks
    private CreateMemberUseCaseImpl createMemberUseCase;

    @Captor
    private ArgumentCaptor<Member> memberCaptor;

    private CreateMemberUseCase.CreateMemberCommand validCommand;

    @BeforeEach
    void setUp() {
        validCommand = new CreateMemberUseCase.CreateMemberCommand(
                "12345678",
                "John Doe",
                BigDecimal.valueOf(5000),
                LocalDate.of(2024, 1, 1),
                1L
        );
    }

    @Nested
    @DisplayName("When creating a new member")
    class CreateMemberTests {

        @Test
        @DisplayName("Should create member with all valid fields")
        void shouldCreateMemberWithValidFields() {
            // Given
            given(memberRepository.existsByDocument("12345678")).willReturn(false);
            given(memberRepository.save(any(Member.class))).willAnswer(invocation -> {
                Member member = invocation.getArgument(0);
                return Member.builder()
                        .id(1L)
                        .document(member.getDocument())
                        .name(member.getName())
                        .salary(member.getSalary())
                        .affiliationDate(member.getAffiliationDate())
                        .status(member.getStatus())
                        .userId(member.getUserId())
                        .build();
            });

            // When
            Member result = createMemberUseCase.execute(validCommand);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getDocument()).isEqualTo("12345678");
            assertThat(result.getName()).isEqualTo("John Doe");
            assertThat(result.getSalary()).isEqualByComparingTo(BigDecimal.valueOf(5000));
            assertThat(result.getStatus()).isEqualTo(MemberStatus.ACTIVE);

            then(memberRepository).should().save(memberCaptor.capture());
            Member savedMember = memberCaptor.getValue();
            assertThat(savedMember.getStatus()).isEqualTo(MemberStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should set default affiliation date when not provided")
        void shouldSetDefaultAffiliationDate() {
            // Given
            CreateMemberUseCase.CreateMemberCommand commandWithoutDate = 
                    new CreateMemberUseCase.CreateMemberCommand(
                            "99999999",
                            "Jane Doe",
                            BigDecimal.valueOf(6000),
                            null,
                            1L
                    );

            given(memberRepository.existsByDocument("99999999")).willReturn(false);
            given(memberRepository.save(any(Member.class))).willAnswer(inv -> inv.getArgument(0));

            // When
            Member result = createMemberUseCase.execute(commandWithoutDate);

            // Then
            then(memberRepository).should().save(memberCaptor.capture());
            Member savedMember = memberCaptor.getValue();
            assertThat(savedMember.getAffiliationDate()).isEqualTo(LocalDate.now());
        }

        @Test
        @DisplayName("Should throw exception when document already exists")
        void shouldThrowWhenDocumentExists() {
            // Given
            given(memberRepository.existsByDocument("12345678")).willReturn(true);

            // When/Then
            assertThatThrownBy(() -> createMemberUseCase.execute(validCommand))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("already exists");

            then(memberRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("Should normalize document removing special characters")
        void shouldNormalizeDocument() {
            // Given
            CreateMemberUseCase.CreateMemberCommand commandWithSpecialChars = 
                    new CreateMemberUseCase.CreateMemberCommand(
                            "123-456-78",
                            "Test User",
                            BigDecimal.valueOf(4000),
                            LocalDate.now(),
                            1L
                    );

            given(memberRepository.existsByDocument(anyString())).willReturn(false);
            given(memberRepository.save(any(Member.class))).willAnswer(inv -> inv.getArgument(0));

            // When
            createMemberUseCase.execute(commandWithSpecialChars);

            // Then
            then(memberRepository).should().save(memberCaptor.capture());
            Member savedMember = memberCaptor.getValue();
            assertThat(savedMember.getDocument()).doesNotContain("-");
        }
    }

    @Nested
    @DisplayName("When validating input")
    class ValidationTests {

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Should throw exception when document is null or empty")
        void shouldThrowWhenDocumentInvalid(String document) {
            // Given
            CreateMemberUseCase.CreateMemberCommand invalidCommand = 
                    new CreateMemberUseCase.CreateMemberCommand(
                            document,
                            "John Doe",
                            BigDecimal.valueOf(5000),
                            LocalDate.now(),
                            1L
                    );

            // When/Then
            assertThatThrownBy(() -> createMemberUseCase.execute(invalidCommand))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("document");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"  ", "\t", "\n"})
        @DisplayName("Should throw exception when name is blank")
        void shouldThrowWhenNameBlank(String name) {
            // Given
            CreateMemberUseCase.CreateMemberCommand invalidCommand = 
                    new CreateMemberUseCase.CreateMemberCommand(
                            "12345678",
                            name,
                            BigDecimal.valueOf(5000),
                            LocalDate.now(),
                            1L
                    );

            // When/Then
            assertThatThrownBy(() -> createMemberUseCase.execute(invalidCommand))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("name");
        }

        @ParameterizedTest
        @ValueSource(doubles = {-1000, -1, 0})
        @DisplayName("Should throw exception when salary is not positive")
        void shouldThrowWhenSalaryNotPositive(double salary) {
            // Given
            CreateMemberUseCase.CreateMemberCommand invalidCommand = 
                    new CreateMemberUseCase.CreateMemberCommand(
                            "12345678",
                            "John Doe",
                            BigDecimal.valueOf(salary),
                            LocalDate.now(),
                            1L
                    );

            // When/Then
            assertThatThrownBy(() -> createMemberUseCase.execute(invalidCommand))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("salary");
        }

        @Test
        @DisplayName("Should throw exception when affiliation date is in the future")
        void shouldThrowWhenAffiliationDateInFuture() {
            // Given
            CreateMemberUseCase.CreateMemberCommand invalidCommand = 
                    new CreateMemberUseCase.CreateMemberCommand(
                            "12345678",
                            "John Doe",
                            BigDecimal.valueOf(5000),
                            LocalDate.now().plusDays(1),
                            1L
                    );

            // When/Then
            assertThatThrownBy(() -> createMemberUseCase.execute(invalidCommand))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("future");
        }
    }

    @Nested
    @DisplayName("When calculating member attributes")
    class CalculationTests {

        @Test
        @DisplayName("Should calculate correct max credit amount")
        void shouldCalculateMaxCreditAmount() {
            // Given
            given(memberRepository.existsByDocument("12345678")).willReturn(false);
            given(memberRepository.save(any(Member.class))).willAnswer(inv -> {
                Member m = inv.getArgument(0);
                return m.toBuilder().id(1L).build();
            });

            // When
            Member result = createMemberUseCase.execute(validCommand);

            // Then
            BigDecimal expectedMaxCredit = BigDecimal.valueOf(5000).multiply(BigDecimal.valueOf(4));
            assertThat(result.calculateMaxCreditAmount()).isEqualByComparingTo(expectedMaxCredit);
        }

        @Test
        @DisplayName("Should calculate correct seniority in months")
        void shouldCalculateSeniorityMonths() {
            // Given
            LocalDate oneYearAgo = LocalDate.now().minusMonths(12);
            CreateMemberUseCase.CreateMemberCommand command = 
                    new CreateMemberUseCase.CreateMemberCommand(
                            "11111111",
                            "Senior Member",
                            BigDecimal.valueOf(8000),
                            oneYearAgo,
                            1L
                    );

            given(memberRepository.existsByDocument("11111111")).willReturn(false);
            given(memberRepository.save(any(Member.class))).willAnswer(inv -> {
                Member m = inv.getArgument(0);
                return m.toBuilder().id(1L).build();
            });

            // When
            Member result = createMemberUseCase.execute(command);

            // Then
            assertThat(result.calculateSeniorityMonths()).isGreaterThanOrEqualTo(12);
        }
    }
}
