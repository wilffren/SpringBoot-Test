package com.coopcredit.creditapplication.infrastructure.controllers;

import com.coopcredit.creditapplication.domain.model.User;
import com.coopcredit.creditapplication.domain.model.enums.UserRole;
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

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class MemberControllerIntegrationTest {

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
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String adminToken;

    @BeforeEach
    void setUp() {
        User admin = User.builder()
                .username("admin_" + System.currentTimeMillis())
                .password(passwordEncoder.encode("admin123"))
                .role(UserRole.ROLE_ADMIN)
                .enabled(true)
                .build();
        admin = userRepository.save(admin);
        adminToken = jwtTokenProvider.generateToken(admin);
    }

    @Test
    @DisplayName("Should create a new member")
    void shouldCreateMember() throws Exception {
        Map<String, Object> request = Map.of(
                "document", "DOC" + System.currentTimeMillis(),
                "name", "John Doe",
                "salary", 5000,
                "affiliationDate", "2024-01-01"
        );

        mockMvc.perform(post("/api/members")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.salary").value(5000));
    }

    @Test
    @DisplayName("Should reject request without JWT token")
    void shouldRejectWithoutToken() throws Exception {
        Map<String, Object> request = Map.of(
                "document", "12345678",
                "name", "Test User",
                "salary", 3000,
                "affiliationDate", "2024-01-01"
        );

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should get member by ID")
    void shouldGetMemberById() throws Exception {
        // First create a member
        Map<String, Object> createRequest = Map.of(
                "document", "GET" + System.currentTimeMillis(),
                "name", "Jane Doe",
                "salary", 6000,
                "affiliationDate", "2024-02-01"
        );

        String response = mockMvc.perform(post("/api/members")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long memberId = objectMapper.readTree(response).get("id").asLong();

        // Then get by ID
        mockMvc.perform(get("/api/members/" + memberId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jane Doe"));
    }
}
