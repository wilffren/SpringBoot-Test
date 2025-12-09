package com.coopcredit.creditapplication.application.usecases.member;

import com.coopcredit.creditapplication.domain.exception.NotFoundException;
import com.coopcredit.creditapplication.domain.model.Member;
import com.coopcredit.creditapplication.domain.ports.in.GetMemberUseCase;
import com.coopcredit.creditapplication.domain.ports.out.MemberRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetMemberUseCaseImpl implements GetMemberUseCase {
    
    private final MemberRepositoryPort memberRepository;
    
    public GetMemberUseCaseImpl(MemberRepositoryPort memberRepository) {
        this.memberRepository = memberRepository;
    }
    
    @Override
    public Member execute(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Member", "id", id));
    }
    
    @Override
    public Member executeByDocument(String document) {
        return memberRepository.findByDocument(document)
                .orElseThrow(() -> new NotFoundException("Member", "document", document));
    }
}