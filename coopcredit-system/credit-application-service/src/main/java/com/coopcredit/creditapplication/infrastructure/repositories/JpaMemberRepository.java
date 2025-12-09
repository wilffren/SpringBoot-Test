package com.coopcredit.creditapplication.infrastructure.repositories;

import com.coopcredit.creditapplication.domain.model.enums.MemberStatus;
import com.coopcredit.creditapplication.infrastructure.entities.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaMemberRepository extends JpaRepository<MemberEntity, Long> {
    
    Optional<MemberEntity> findByDocument(String document);
    
    Optional<MemberEntity> findByUserId(Long userId);
    
    List<MemberEntity> findByStatus(MemberStatus status);
    
    boolean existsByDocument(String document);
}
