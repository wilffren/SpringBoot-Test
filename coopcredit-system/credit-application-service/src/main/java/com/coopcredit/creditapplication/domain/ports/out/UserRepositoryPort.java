package com.coopcredit.creditapplication.domain.ports.out;

import com.coopcredit.creditapplication.domain.model.User;
import java.util.Optional;

/**
 * Port for user repository operations.
 */
public interface UserRepositoryPort {
    
    User save(User user);
    
    Optional<User> findById(Long id);
    
    Optional<User> findByUsername(String username);
    
    boolean existsByUsername(String username);
    
    void deleteById(Long id);
}