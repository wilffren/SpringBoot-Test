package com.coopcredit.creditapplication.infrastructure.mappers;

import com.coopcredit.creditapplication.domain.model.CreditApplication;
import com.coopcredit.creditapplication.infrastructure.entities.CreditApplicationEntity;
import org.springframework.stereotype.Component;

@Component
public class CreditApplicationMapper {
    
    public CreditApplication toDomain(CreditApplicationEntity entity) {
        if (entity == null) return null;
        
        CreditApplication application = new CreditApplication();
        application.setId(entity.getId());
        application.setMemberId(entity.getMemberId());
        application.setRequestedAmount(entity.getRequestedAmount());
        application.setTermMonths(entity.getTermMonths());
        application.setProposedRate(entity.getProposedRate());
        application.setApplicationDate(entity.getApplicationDate());
        application.setStatus(entity.getStatus());
        application.setCreatedAt(entity.getCreatedAt());
        application.setUpdatedAt(entity.getUpdatedAt());
        return application;
    }
    
    public CreditApplicationEntity toEntity(CreditApplication domain) {
        if (domain == null) return null;
        
        CreditApplicationEntity entity = new CreditApplicationEntity();
        entity.setId(domain.getId());
        entity.setMemberId(domain.getMemberId());
        entity.setRequestedAmount(domain.getRequestedAmount());
        entity.setTermMonths(domain.getTermMonths());
        entity.setProposedRate(domain.getProposedRate());
        entity.setApplicationDate(domain.getApplicationDate());
        entity.setStatus(domain.getStatus());
        return entity;
    }
}