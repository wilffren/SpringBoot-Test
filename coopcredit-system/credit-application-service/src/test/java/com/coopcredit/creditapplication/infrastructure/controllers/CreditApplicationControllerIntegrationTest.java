package com.coopcredit.creditapplication.infrastructure.controllers;

import com.coopcredit.creditapplication.domain.model.Member;
import com.coopcredit.creditapplication.domain.model.User;
import com.coopcredit.creditapplication.domain.model.enums.MemberStatus;
import com.coopcredit.creditapplication.domain.model.enums.UserRole;
import com.coopcredit.creditapplication.domain.ports.out.MemberRepositoryPort;
import com.coopcredit.creditapplication.domain.ports.out.UserRepositoryPort;
import com.coopcredit.creditapplication.infrastructure.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class CreditApplicationControllerIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("coopcredit_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.flyway.enabled", () -> "false");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepositoryPort userRepository;

    @Autowired
    private MemberRepositoryPort memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String analystToken;
    private Long memberId;

    @BeforeEach
    void setUp() {
        User analyst = User.builder()
                .username("analyst_" + System.currentTimeMillis())
                .password(passwordEncoder.encode("analyst123"))
                .role(UserRole.ROLE_ANALYST)
                .enabled(true)
                .build();
        analyst = userRepository.save(analyst);
        analystToken = jwtTokenProvider.generateToken(analyst);

        Member member = Member.builder()
                .document("CREDIT" + System.currentTimeMillis())
                .name("Credit Test Member")
                .salary(BigDecimal.valueOf(5000))
                .affiliationDate(LocalDate.now().minusMonths(12))
                .status(MemberStatus.ACTIVE)
                .userId(analyst.getId())
                .build();
        member = memberRepository.save(member);
        memberId = member.getId();
    }

    @Test
    @DisplayName("Should create a credit application")
    void shouldCreateCreditApplication() throws Exception {
        Map<String, Object> request = Map.of(
                "memberId", memberId,
                "requestedAmount", 10000,
                "termMonths", 12,
                "proposedRate", 1.5
        );

        mockMvc.perform(post("/api/credit-applications")
                        .header("Authorization", "Bearer " + analystToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").value(memberId))
                .andExpect(jsonPath("$.requestedAmount").value(10000))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("Should list credit applications")
    void shouldListCreditApplications() throws Exception {
        mockMvc.perform(get("/api/credit-applications")
                        .header("Authorization", "Bearer " + analystToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Should get credit application by ID")
    void shouldGetCreditApplicationById() throws Exception {
        // Create application first
        Map<String, Object> createRequest = Map.of(
                "memberId", memberId,
                "requestedAmount", 8000,
                "termMonths", 6,
                "proposedRate", 1.2
        );

        String response = mockMvc.perform(post("/api/credit-applications")
                        .header("Authorization", "Bearer " + analystToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long applicationId = objectMapper.readTree(response).get("id").asLong();

        // Get by ID
        mockMvc.perform(get("/api/credit-applications/" + applicationId)
                        .header("Authorization", "Bearer " + analystToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestedAmount").value(8000));
    }
}
