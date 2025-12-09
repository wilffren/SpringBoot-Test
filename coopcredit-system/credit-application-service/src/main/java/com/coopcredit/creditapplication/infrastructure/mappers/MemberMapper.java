package com.coopcredit.creditapplication.infrastructure.mappers;

import com.coopcredit.creditapplication.domain.model.Member;
import com.coopcredit.creditapplication.infrastructure.entities.MemberEntity;
import org.springframework.stereotype.Component;

@Component
public class MemberMapper {
    
    public Member toDomain(MemberEntity entity) {
        if (entity == null) return null;
        
        Member member = new Member();
        member.setId(entity.getId());
        member.setDocument(entity.getDocument());
        member.setName(entity.getName());
        member.setSalary(entity.getSalary());
        member.setAffiliationDate(entity.getAffiliationDate());
        member.setStatus(entity.getStatus());
        member.setUserId(entity.getUserId());
        member.setCreatedAt(entity.getCreatedAt());
        member.setUpdatedAt(entity.getUpdatedAt());
        return member;
    }
    
    public MemberEntity toEntity(Member domain) {
        if (domain == null) return null;
        
        MemberEntity entity = new MemberEntity();
        entity.setId(domain.getId());
        entity.setDocument(domain.getDocument());
        entity.setName(domain.getName());
        entity.setSalary(domain.getSalary());
        entity.setAffiliationDate(domain.getAffiliationDate());
        entity.setStatus(domain.getStatus());
        entity.setUserId(domain.getUserId());
        return entity;
    }
}