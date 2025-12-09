package com.coopcredit.creditapplication.infrastructure.adapters.persistence;

import com.coopcredit.creditapplication.domain.model.CreditApplication;
import com.coopcredit.creditapplication.domain.model.enums.ApplicationStatus;
import com.coopcredit.creditapplication.domain.ports.out.CreditApplicationRepositoryPort;
import com.coopcredit.creditapplication.infrastructure.mappers.CreditApplicationMapper;
import com.coopcredit.creditapplication.infrastructure.repositories.JpaCreditApplicationRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CreditApplicationRepositoryAdapter implements CreditApplicationRepositoryPort {
    
    private final JpaCreditApplicationRepository jpaRepository;
    private final CreditApplicationMapper mapper;
    
    public CreditApplicationRepositoryAdapter(JpaCreditApplicationRepository jpaRepository, 
                                               CreditApplicationMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }
    
    @Override
    public CreditApplication save(CreditApplication creditApplication) {
        var entity = mapper.toEntity(creditApplication);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
    
    @Override
    public Optional<CreditApplication> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }
    
    @Override
    public List<CreditApplication> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).collect(Collectors.toList());
    }
    
    @Override
    public List<CreditApplication> findByMemberId(Long memberId) {
        return jpaRepository.findByMemberId(memberId).stream().map(mapper::toDomain).collect(Collectors.toList());
    }
    
    @Override
    public List<CreditApplication> findByStatus(ApplicationStatus status) {
        return jpaRepository.findByStatus(status).stream().map(mapper::toDomain).collect(Collectors.toList());
    }
    
    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}