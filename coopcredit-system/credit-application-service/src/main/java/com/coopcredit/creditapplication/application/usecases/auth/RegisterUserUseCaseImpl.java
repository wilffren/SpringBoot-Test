package com.coopcredit.creditapplication.application.usecases.auth;

import com.coopcredit.creditapplication.domain.exception.ConflictException;
import com.coopcredit.creditapplication.domain.model.User;
import com.coopcredit.creditapplication.domain.ports.in.RegisterUserUseCase;
import com.coopcredit.creditapplication.domain.ports.out.UserRepositoryPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RegisterUserUseCaseImpl implements RegisterUserUseCase {
    
    private final UserRepositoryPort userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public RegisterUserUseCaseImpl(UserRepositoryPort userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    public User execute(RegisterUserCommand command) {
        // Check if username already exists
        if (userRepository.existsByUsername(command.username())) {
            throw new ConflictException("User", "username", command.username());
        }
        
        User user = User.builder()
                .username(command.username())
                .password(passwordEncoder.encode(command.password()))
                .role(command.role())
                .enabled(true)
                .build();
        
        return userRepository.save(user);
    }
}