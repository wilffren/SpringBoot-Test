package com.coopcredit.creditapplication.domain.ports.in;

import com.coopcredit.creditapplication.domain.model.User;
import com.coopcredit.creditapplication.domain.model.enums.UserRole;

/**
 * Port for registering a new user use case.
 */
public interface RegisterUserUseCase {
    
    record RegisterUserCommand(
        String username,
        String password,
        UserRole role
    ) {}
    
    User execute(RegisterUserCommand command);
}