package com.coopcredit.creditapplication.infrastructure.security;

import com.coopcredit.creditapplication.domain.model.User;
import com.coopcredit.creditapplication.domain.model.enums.UserRole;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtTokenProvider Unit Tests")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    private static final String SECRET_KEY = "mySecretKeyForTestingPurposes12345678901234567890";
    private static final long EXPIRATION_TIME = 86400000L; // 24 hours

    private User testUser;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "secretKey", SECRET_KEY);
        ReflectionTestUtils.setField(jwtTokenProvider, "expirationTime", EXPIRATION_TIME);

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("encodedPassword")
                .role(UserRole.ROLE_ANALYST)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("When generating tokens")
    class TokenGenerationTests {

        @Test
        @DisplayName("Should generate valid JWT token for user")
        void shouldGenerateValidToken() {
            // When
            String token = jwtTokenProvider.generateToken(testUser);

            // Then
            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
            assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
        }

        @ParameterizedTest
        @EnumSource(UserRole.class)
        @DisplayName("Should include role in token for all user roles")
        void shouldIncludeRoleInToken(UserRole role) {
            // Given
            User userWithRole = testUser.toBuilder().role(role).build();

            // When
            String token = jwtTokenProvider.generateToken(userWithRole);

            // Then
            assertThat(token).isNotNull();
            String extractedRole = jwtTokenProvider.extractRole(token);
            assertThat(extractedRole).isEqualTo(role.name());
        }

        @Test
        @DisplayName("Should generate different tokens for different users")
        void shouldGenerateDifferentTokensForDifferentUsers() {
            // Given
            User anotherUser = User.builder()
                    .id(2L)
                    .username("anotheruser")
                    .password("password")
                    .role(UserRole.ROLE_MEMBER)
                    .enabled(true)
                    .build();

            // When
            String token1 = jwtTokenProvider.generateToken(testUser);
            String token2 = jwtTokenProvider.generateToken(anotherUser);

            // Then
            assertThat(token1).isNotEqualTo(token2);
        }

        @Test
        @DisplayName("Should include correct expiration time")
        void shouldIncludeCorrectExpiration() {
            // When
            String token = jwtTokenProvider.generateToken(testUser);

            // Then
            assertThat(jwtTokenProvider.isTokenValid(token, testUser)).isTrue();
            assertThat(jwtTokenProvider.getExpirationTime()).isEqualTo(EXPIRATION_TIME);
        }
    }

    @Nested
    @DisplayName("When extracting claims from tokens")
    class ClaimExtractionTests {

        @Test
        @DisplayName("Should extract username from token")
        void shouldExtractUsername() {
            // Given
            String token = jwtTokenProvider.generateToken(testUser);

            // When
            String username = jwtTokenProvider.extractUsername(token);

            // Then
            assertThat(username).isEqualTo("testuser");
        }

        @Test
        @DisplayName("Should extract role from token")
        void shouldExtractRole() {
            // Given
            String token = jwtTokenProvider.generateToken(testUser);

            // When
            String role = jwtTokenProvider.extractRole(token);

            // Then
            assertThat(role).isEqualTo(UserRole.ROLE_ANALYST.name());
        }

        @Test
        @DisplayName("Should extract user ID from token")
        void shouldExtractUserId() {
            // Given
            String token = jwtTokenProvider.generateToken(testUser);

            // When
            Long userId = jwtTokenProvider.extractUserId(token);

            // Then
            assertThat(userId).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("When validating tokens")
    class TokenValidationTests {

        @Test
        @DisplayName("Should validate correct token")
        void shouldValidateCorrectToken() {
            // Given
            String token = jwtTokenProvider.generateToken(testUser);

            // When
            boolean isValid = jwtTokenProvider.isTokenValid(token, testUser);

            // Then
            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("Should reject token with wrong username")
        void shouldRejectTokenWithWrongUsername() {
            // Given
            String token = jwtTokenProvider.generateToken(testUser);
            User differentUser = testUser.toBuilder().username("differentuser").build();

            // When
            boolean isValid = jwtTokenProvider.isTokenValid(token, differentUser);

            // Then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("Should reject malformed token")
        void shouldRejectMalformedToken() {
            // Given
            String malformedToken = "not.a.valid.jwt.token";

            // When/Then
            assertThatThrownBy(() -> jwtTokenProvider.extractUsername(malformedToken))
                    .isInstanceOf(MalformedJwtException.class);
        }

        @Test
        @DisplayName("Should reject token with invalid signature")
        void shouldRejectTokenWithInvalidSignature() {
            // Given
            String token = jwtTokenProvider.generateToken(testUser);
            String tamperedToken = token.substring(0, token.lastIndexOf('.') + 1) + "invalidSignature";

            // When/Then
            assertThatThrownBy(() -> jwtTokenProvider.extractUsername(tamperedToken))
                    .isInstanceOf(SignatureException.class);
        }

        @Test
        @DisplayName("Should reject expired token")
        void shouldRejectExpiredToken() {
            // Given - Create provider with very short expiration
            JwtTokenProvider shortLivedProvider = new JwtTokenProvider();
            ReflectionTestUtils.setField(shortLivedProvider, "secretKey", SECRET_KEY);
            ReflectionTestUtils.setField(shortLivedProvider, "expirationTime", 1L); // 1ms

            String token = shortLivedProvider.generateToken(testUser);

            // Wait for token to expire
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // When/Then
            assertThatThrownBy(() -> shortLivedProvider.extractUsername(token))
                    .isInstanceOf(ExpiredJwtException.class);
        }

        @Test
        @DisplayName("Should reject null token")
        void shouldRejectNullToken() {
            // When/Then
            assertThatThrownBy(() -> jwtTokenProvider.extractUsername(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should reject empty token")
        void shouldRejectEmptyToken() {
            // When/Then
            assertThatThrownBy(() -> jwtTokenProvider.extractUsername(""))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("When handling edge cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle user with special characters in username")
        void shouldHandleSpecialCharactersInUsername() {
            // Given
            User specialUser = testUser.toBuilder()
                    .username("user+test@example.com")
                    .build();

            // When
            String token = jwtTokenProvider.generateToken(specialUser);
            String extractedUsername = jwtTokenProvider.extractUsername(token);

            // Then
            assertThat(extractedUsername).isEqualTo("user+test@example.com");
        }

        @Test
        @DisplayName("Should handle very long username")
        void shouldHandleVeryLongUsername() {
            // Given
            String longUsername = "a".repeat(255);
            User longUsernameUser = testUser.toBuilder()
                    .username(longUsername)
                    .build();

            // When
            String token = jwtTokenProvider.generateToken(longUsernameUser);
            String extractedUsername = jwtTokenProvider.extractUsername(token);

            // Then
            assertThat(extractedUsername).isEqualTo(longUsername);
        }

        @Test
        @DisplayName("Should generate consistent tokens for same user data")
        void shouldBeConsistentForSameUser() {
            // When
            String token1 = jwtTokenProvider.generateToken(testUser);
            String token2 = jwtTokenProvider.generateToken(testUser);

            // Then - Tokens should be different (different timestamps) but extract same data
            assertThat(token1).isNotEqualTo(token2);
            assertThat(jwtTokenProvider.extractUsername(token1))
                    .isEqualTo(jwtTokenProvider.extractUsername(token2));
            assertThat(jwtTokenProvider.extractRole(token1))
                    .isEqualTo(jwtTokenProvider.extractRole(token2));
        }
    }
}
