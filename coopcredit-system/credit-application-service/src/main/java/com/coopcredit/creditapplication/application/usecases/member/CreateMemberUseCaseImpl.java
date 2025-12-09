package com.coopcredit.creditapplication.application.usecases.member;

import com.coopcredit.creditapplication.domain.exception.ConflictException;
import com.coopcredit.creditapplication.domain.model.Member;
import com.coopcredit.creditapplication.domain.model.enums.MemberStatus;
import com.coopcredit.creditapplication.domain.ports.in.CreateMemberUseCase;
import com.coopcredit.creditapplication.domain.ports.out.MemberRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreateMemberUseCaseImpl implements CreateMemberUseCase {
    
    private final MemberRepositoryPort memberRepository;
    
    public CreateMemberUseCaseImpl(MemberRepositoryPort memberRepository) {
        this.memberRepository = memberRepository;
    }
    
    @Override
    public Member execute(CreateMemberCommand command) {
        // Check if member with same document already exists
        if (memberRepository.existsByDocument(command.document())) {
            throw new ConflictException("Member", "document", command.document());
        }
        
        Member member = Member.builder()
                .document(command.document())
                .name(command.name())
                .salary(command.salary())
                .affiliationDate(command.affiliationDate())
                .status(MemberStatus.ACTIVE)
                .build();
        
        return memberRepository.save(member);
    }
}