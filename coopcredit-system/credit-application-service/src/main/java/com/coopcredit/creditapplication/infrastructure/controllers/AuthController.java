package com.coopcredit.creditapplication.infrastructure.controllers;

import com.coopcredit.creditapplication.domain.model.User;
import com.coopcredit.creditapplication.domain.model.enums.UserRole;
import com.coopcredit.creditapplication.domain.ports.in.AuthenticateUserUseCase;
import com.coopcredit.creditapplication.domain.ports.in.RegisterUserUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User authentication and registration")
public class AuthController {
    
    private final AuthenticateUserUseCase authenticateUserUseCase;
    private final RegisterUserUseCase registerUserUseCase;
    
    public AuthController(AuthenticateUserUseCase authenticateUserUseCase, 
                          RegisterUserUseCase registerUserUseCase) {
        this.authenticateUserUseCase = authenticateUserUseCase;
        this.registerUserUseCase = registerUserUseCase;
    }
    
    @PostMapping("/login")
    @Operation(summary = "Authenticate user and get JWT token")
    public ResponseEntity<AuthenticateUserUseCase.AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        var authRequest = new AuthenticateUserUseCase.AuthRequest(request.username(), request.password());
        var response = authenticateUserUseCase.authenticate(authRequest);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        var command = new RegisterUserUseCase.RegisterUserCommand(
                request.username(), 
                request.password(), 
                request.role()
        );
        User user = registerUserUseCase.execute(command);
        return ResponseEntity.ok(new UserResponse(user.getId(), user.getUsername(), user.getRole().name()));
    }
    
    // DTOs
    record LoginRequest(
            @NotBlank String username,
            @NotBlank String password
    ) {}
    
    record RegisterRequest(
            @NotBlank @Size(min = 3, max = 50) String username,
            @NotBlank @Size(min = 6) String password,
            UserRole role
    ) {}
    
    record UserResponse(Long id, String username, String role) {}
}