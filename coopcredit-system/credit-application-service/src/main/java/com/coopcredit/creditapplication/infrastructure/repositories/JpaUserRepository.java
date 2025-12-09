package com.coopcredit.creditapplication.infrastructure.repositories;

import com.coopcredit.creditapplication.infrastructure.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaUserRepository extends JpaRepository<UserEntity, Long> {
    
    Optional<UserEntity> findByUsername(String username);
    
    boolean existsByUsername(String username);
}
