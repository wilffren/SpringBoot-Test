package com.coopcredit.creditapplication.application.usecases.member;

import com.coopcredit.creditapplication.domain.exception.NotFoundException;
import com.coopcredit.creditapplication.domain.model.Member;
import com.coopcredit.creditapplication.domain.ports.in.UpdateMemberUseCase;
import com.coopcredit.creditapplication.domain.ports.out.MemberRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UpdateMemberUseCaseImpl implements UpdateMemberUseCase {
    
    private final MemberRepositoryPort memberRepository;
    
    public UpdateMemberUseCaseImpl(MemberRepositoryPort memberRepository) {
        this.memberRepository = memberRepository;
    }
    
    @Override
    public Member execute(UpdateMemberCommand command) {
        Member member = memberRepository.findById(command.id())
                .orElseThrow(() -> new NotFoundException("Member", "id", command.id()));
        
        if (command.name() != null) {
            member.setName(command.name());
        }
        if (command.salary() != null) {
            member.setSalary(command.salary());
        }
        if (command.status() != null) {
            member.setStatus(command.status());
        }
        
        return memberRepository.save(member);
    }
}