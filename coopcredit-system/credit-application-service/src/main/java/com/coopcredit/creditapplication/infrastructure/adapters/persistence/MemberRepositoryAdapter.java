package com.coopcredit.creditapplication.infrastructure.adapters.persistence;

import com.coopcredit.creditapplication.domain.model.Member;
import com.coopcredit.creditapplication.domain.model.enums.MemberStatus;
import com.coopcredit.creditapplication.domain.ports.out.MemberRepositoryPort;
import com.coopcredit.creditapplication.infrastructure.mappers.MemberMapper;
import com.coopcredit.creditapplication.infrastructure.repositories.JpaMemberRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class MemberRepositoryAdapter implements MemberRepositoryPort {
    
    private final JpaMemberRepository jpaRepository;
    private final MemberMapper mapper;
    
    public MemberRepositoryAdapter(JpaMemberRepository jpaRepository, MemberMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }
    
    @Override
    public Member save(Member member) {
        var entity = mapper.toEntity(member);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
    
    @Override
    public Optional<Member> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }
    
    @Override
    public Optional<Member> findByDocument(String document) {
        return jpaRepository.findByDocument(document).map(mapper::toDomain);
    }
    
    @Override
    public Optional<Member> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId).map(mapper::toDomain);
    }
    
    @Override
    public List<Member> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).collect(Collectors.toList());
    }
    
    @Override
    public List<Member> findByStatus(MemberStatus status) {
        return jpaRepository.findByStatus(status).stream().map(mapper::toDomain).collect(Collectors.toList());
    }
    
    @Override
    public boolean existsByDocument(String document) {
        return jpaRepository.existsByDocument(document);
    }
    
    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}