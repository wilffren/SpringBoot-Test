package com.coopcredit.creditapplication.domain.ports.in;

/**
 * Port for user authentication use case.
 */
public interface AuthenticateUserUseCase {
    
    record AuthRequest(String username, String password) {}
    
    record AuthResponse(String token, String username, String role, Long expiresIn) {}
    
    AuthResponse authenticate(AuthRequest request);
}