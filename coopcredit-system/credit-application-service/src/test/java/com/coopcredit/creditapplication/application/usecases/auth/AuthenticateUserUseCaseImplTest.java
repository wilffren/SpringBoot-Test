package com.coopcredit.creditapplication.application.usecases.auth;

import com.coopcredit.creditapplication.domain.model.User;
import com.coopcredit.creditapplication.domain.model.enums.UserRole;
import com.coopcredit.creditapplication.domain.ports.in.AuthenticateUserUseCase;
import com.coopcredit.creditapplication.domain.ports.out.UserRepositoryPort;
import com.coopcredit.creditapplication.infrastructure.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticateUserUseCase Unit Tests")
class AuthenticateUserUseCaseImplTest {

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthenticateUserUseCaseImpl authenticateUserUseCase;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private User testUser;
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "password123";
    private static final String ENCODED_PASSWORD = "$2a$10$encoded";
    private static final String JWT_TOKEN = "eyJhbGciOiJIUzI1NiJ9.test.token";

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username(TEST_USERNAME)
                .password(ENCODED_PASSWORD)
                .role(UserRole.ROLE_ANALYST)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("When authenticating user")
    class AuthenticateTests {

        @Test
        @DisplayName("Should return auth response with token when credentials are valid")
        void shouldReturnAuthResponseWhenCredentialsValid() {
            // Given
            var request = new AuthenticateUserUseCase.AuthRequest(TEST_USERNAME, TEST_PASSWORD);
            
            given(userRepository.findByUsername(TEST_USERNAME)).willReturn(Optional.of(testUser));
            given(passwordEncoder.matches(TEST_PASSWORD, ENCODED_PASSWORD)).willReturn(true);
            given(jwtTokenProvider.generateToken(testUser)).willReturn(JWT_TOKEN);
            given(jwtTokenProvider.getExpirationTime()).willReturn(86400000L);

            // When
            var response = authenticateUserUseCase.authenticate(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.token()).isEqualTo(JWT_TOKEN);
            assertThat(response.username()).isEqualTo(TEST_USERNAME);
            assertThat(response.role()).isEqualTo(UserRole.ROLE_ANALYST.name());
            assertThat(response.expiresIn()).isEqualTo(86400000L);

            then(userRepository).should(times(1)).findByUsername(TEST_USERNAME);
            then(passwordEncoder).should(times(1)).matches(TEST_PASSWORD, ENCODED_PASSWORD);
            then(jwtTokenProvider).should(times(1)).generateToken(testUser);
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            // Given
            var request = new AuthenticateUserUseCase.AuthRequest("nonexistent", TEST_PASSWORD);
            given(userRepository.findByUsername("nonexistent")).willReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> authenticateUserUseCase.authenticate(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Invalid credentials");

            then(passwordEncoder).shouldHaveNoInteractions();
            then(jwtTokenProvider).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("Should throw exception when password does not match")
        void shouldThrowExceptionWhenPasswordDoesNotMatch() {
            // Given
            var request = new AuthenticateUserUseCase.AuthRequest(TEST_USERNAME, "wrongpassword");
            
            given(userRepository.findByUsername(TEST_USERNAME)).willReturn(Optional.of(testUser));
            given(passwordEncoder.matches("wrongpassword", ENCODED_PASSWORD)).willReturn(false);

            // When/Then
            assertThatThrownBy(() -> authenticateUserUseCase.authenticate(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Invalid credentials");

            then(jwtTokenProvider).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("Should throw exception when user is disabled")
        void shouldThrowExceptionWhenUserDisabled() {
            // Given
            testUser = User.builder()
                    .id(1L)
                    .username(TEST_USERNAME)
                    .password(ENCODED_PASSWORD)
                    .role(UserRole.ROLE_MEMBER)
                    .enabled(false)
                    .build();
            
            var request = new AuthenticateUserUseCase.AuthRequest(TEST_USERNAME, TEST_PASSWORD);
            
            given(userRepository.findByUsername(TEST_USERNAME)).willReturn(Optional.of(testUser));
            given(passwordEncoder.matches(TEST_PASSWORD, ENCODED_PASSWORD)).willReturn(true);

            // When/Then
            assertThatThrownBy(() -> authenticateUserUseCase.authenticate(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("disabled");
        }
    }

    @Nested
    @DisplayName("When validating input")
    class ValidationTests {

        @Test
        @DisplayName("Should throw exception when username is null")
        void shouldThrowExceptionWhenUsernameNull() {
            // Given
            var request = new AuthenticateUserUseCase.AuthRequest(null, TEST_PASSWORD);

            // When/Then
            assertThatThrownBy(() -> authenticateUserUseCase.authenticate(request))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception when password is empty")
        void shouldThrowExceptionWhenPasswordEmpty() {
            // Given
            var request = new AuthenticateUserUseCase.AuthRequest(TEST_USERNAME, "");

            // When/Then
            assertThatThrownBy(() -> authenticateUserUseCase.authenticate(request))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
