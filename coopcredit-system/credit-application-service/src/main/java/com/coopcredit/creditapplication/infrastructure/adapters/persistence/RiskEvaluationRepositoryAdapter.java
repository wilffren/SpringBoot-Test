package com.coopcredit.creditapplication.infrastructure.adapters.persistence;

import com.coopcredit.creditapplication.domain.model.RiskEvaluation;
import com.coopcredit.creditapplication.domain.ports.out.RiskEvaluationRepositoryPort;
import com.coopcredit.creditapplication.infrastructure.mappers.RiskEvaluationMapper;
import com.coopcredit.creditapplication.infrastructure.repositories.JpaRiskEvaluationRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RiskEvaluationRepositoryAdapter implements RiskEvaluationRepositoryPort {
    
    private final JpaRiskEvaluationRepository jpaRepository;
    private final RiskEvaluationMapper mapper;
    
    public RiskEvaluationRepositoryAdapter(JpaRiskEvaluationRepository jpaRepository, 
                                            RiskEvaluationMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }
    
    @Override
    public RiskEvaluation save(RiskEvaluation riskEvaluation) {
        var entity = mapper.toEntity(riskEvaluation);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
    
    @Override
    public Optional<RiskEvaluation> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }
    
    @Override
    public Optional<RiskEvaluation> findByCreditApplicationId(Long creditApplicationId) {
        return jpaRepository.findByCreditApplicationId(creditApplicationId).map(mapper::toDomain);
    }
    
    @Override
    public boolean existsByCreditApplicationId(Long creditApplicationId) {
        return jpaRepository.existsByCreditApplicationId(creditApplicationId);
    }
    
    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}