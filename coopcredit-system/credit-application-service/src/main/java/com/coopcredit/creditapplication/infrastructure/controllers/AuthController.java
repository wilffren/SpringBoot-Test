package com.coopcredit.creditapplication.infrastructure.controllers;

import com.coopcredit.creditapplication.domain.model.User;
import com.coopcredit.creditapplication.domain.model.enums.UserRole;
import com.coopcredit.creditapplication.domain.ports.in.AuthenticateUserUseCase;
import com.coopcredit.creditapplication.domain.ports.in.RegisterUserUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "1. Authentication", description = "Public endpoints - No token required")
public class AuthController {

        private final AuthenticateUserUseCase authenticateUserUseCase;
        private final RegisterUserUseCase registerUserUseCase;

        public AuthController(AuthenticateUserUseCase authenticateUserUseCase,
                        RegisterUserUseCase registerUserUseCase) {
                this.authenticateUserUseCase = authenticateUserUseCase;
                this.registerUserUseCase = registerUserUseCase;
        }

        @PostMapping("/login")
        @Operation(summary = "Login and get JWT token", description = "Authenticate with username/password to receive JWT token. Use this token in the Authorize button.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Login successful - Copy the token from response"),
                        @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content)
        })
        @SecurityRequirements // Empty = No security required (public endpoint)
        public ResponseEntity<AuthenticateUserUseCase.AuthResponse> login(@Valid @RequestBody LoginRequest request) {
                var authRequest = new AuthenticateUserUseCase.AuthRequest(request.username(), request.password());
                var response = authenticateUserUseCase.authenticate(authRequest);
                return ResponseEntity.ok(response);
        }

        @PostMapping("/register")
        @Operation(summary = "Register a new user", description = """
                        Create a new user account with one of three available roles:

                        **ROLE_ADMIN**: Full access to all operations
                        - ✅ Manage members (create, update, delete)
                        - ✅ Manage credit applications (all operations)
                        - ✅ Evaluate credit applications
                        - ✅ Access all actuator endpoints
                        - ✅ Delete members and applications

                        **ROLE_ANALYST**: Credit analyst permissions
                        - ✅ Manage members (create, update)
                        - ✅ View all credit applications
                        - ✅ Evaluate credit applications
                        - ❌ Cannot delete members or applications

                        **ROLE_MEMBER**: Basic member permissions
                        - ✅ View member information
                        - ✅ Create credit applications
                        - ✅ View own credit applications
                        - ❌ Cannot evaluate applications
                        - ❌ Cannot manage other members

                        Example request:
                        ```json
                        {
                          "username": "admin",
                          "password": "admin123",
                          "role": "ROLE_ADMIN"
                        }
                        ```
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "User registered successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid input or username already exists", content = @Content)
        })
        @SecurityRequirements // Empty = No security required (public endpoint)
        public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
                var command = new RegisterUserUseCase.RegisterUserCommand(
                                request.username(),
                                request.password(),
                                request.role());
                User user = registerUserUseCase.execute(command);
                return ResponseEntity.ok(new UserResponse(user.getId(), user.getUsername(), user.getRole().name()));
        }

        // DTOs
        record LoginRequest(
                        @NotBlank String username,
                        @NotBlank String password) {
        }

        record RegisterRequest(
                        @NotBlank @Size(min = 3, max = 50) String username,
                        @NotBlank @Size(min = 6) String password,
                        UserRole role) {
        }

        record UserResponse(Long id, String username, String role) {
        }
}