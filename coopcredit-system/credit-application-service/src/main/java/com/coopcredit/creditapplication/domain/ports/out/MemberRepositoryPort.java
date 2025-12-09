package com.coopcredit.creditapplication.domain.ports.out;

import com.coopcredit.creditapplication.domain.model.Member;
import com.coopcredit.creditapplication.domain.model.enums.MemberStatus;
import java.util.List;
import java.util.Optional;

/**
 * Port for member repository operations.
 */
public interface MemberRepositoryPort {
    
    Member save(Member member);
    
    Optional<Member> findById(Long id);
    
    Optional<Member> findByDocument(String document);
    
    Optional<Member> findByUserId(Long userId);
    
    List<Member> findAll();
    
    List<Member> findByStatus(MemberStatus status);
    
    boolean existsByDocument(String document);
    
    void deleteById(Long id);
}