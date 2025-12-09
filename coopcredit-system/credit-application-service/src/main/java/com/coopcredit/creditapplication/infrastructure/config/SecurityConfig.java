package com.coopcredit.creditapplication.infrastructure.config;

import com.coopcredit.creditapplication.infrastructure.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // ==================== PUBLIC ENDPOINTS ====================
                        // Authentication - Only register and login are public
                        .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()

                        // Swagger UI - Only for viewing documentation
                        .requestMatchers("/swagger-ui.html").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-resources/**").permitAll()
                        .requestMatchers("/webjars/**").permitAll()

                        // Actuator health only
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/actuator/info").permitAll()
                        .requestMatchers("/actuator/prometheus").permitAll()
                        .requestMatchers("/actuator/metrics/**").permitAll()

                        // ==================== PROTECTED ENDPOINTS ====================
                        // All other actuator endpoints require authentication
                        .requestMatchers("/actuator/**").hasRole("ADMIN")

                        // Member management - Analysts and Admins only
                        .requestMatchers(HttpMethod.POST, "/api/members/**").hasAnyRole("ANALYST", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/members/**").hasAnyRole("ANALYST", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/members/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/members/**").hasAnyRole("MEMBER", "ANALYST", "ADMIN")

                        // Credit applications
                        .requestMatchers(HttpMethod.POST, "/api/credit-applications")
                        .hasAnyRole("MEMBER", "ANALYST", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/credit-applications/*/evaluate")
                        .hasAnyRole("ANALYST", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/credit-applications/**")
                        .hasAnyRole("MEMBER", "ANALYST", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/credit-applications/**").hasAnyRole("ANALYST", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/credit-applications/**").hasRole("ADMIN")

                        // Risk evaluations - Analysts and Admins only
                        .requestMatchers("/api/evaluations/**").hasAnyRole("ANALYST", "ADMIN")

                        // ==================== DENY ALL OTHER REQUESTS ====================
                        // Any request not explicitly configured above requires authentication
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // Return 401 for unauthenticated requests to API endpoints
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json");
                            response.setStatus(401);
                            response.getWriter().write(
                                    "{\"error\":\"Unauthorized\",\"message\":\"JWT token is required. Please login first at /api/auth/login\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setContentType("application/json");
                            response.setStatus(403);
                            response.getWriter().write(
                                    "{\"error\":\"Forbidden\",\"message\":\"You don't have permission to access this resource\"}");
                        }));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}