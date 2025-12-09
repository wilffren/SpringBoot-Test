package com.coopcredit.creditapplication.infrastructure.repositories;

import com.coopcredit.creditapplication.domain.model.enums.ApplicationStatus;
import com.coopcredit.creditapplication.infrastructure.entities.CreditApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaCreditApplicationRepository extends JpaRepository<CreditApplicationEntity, Long> {
    
    List<CreditApplicationEntity> findByMemberId(Long memberId);
    
    List<CreditApplicationEntity> findByStatus(ApplicationStatus status);
}