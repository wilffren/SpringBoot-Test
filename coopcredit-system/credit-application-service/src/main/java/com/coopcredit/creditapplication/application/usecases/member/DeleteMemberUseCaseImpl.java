package com.coopcredit.creditapplication.application.usecases.member;

import com.coopcredit.creditapplication.domain.exception.BusinessRuleException;
import com.coopcredit.creditapplication.domain.exception.NotFoundException;
import com.coopcredit.creditapplication.domain.model.CreditApplication;
import com.coopcredit.creditapplication.domain.model.Member;
import com.coopcredit.creditapplication.domain.model.enums.ApplicationStatus;
import com.coopcredit.creditapplication.domain.ports.in.DeleteMemberUseCase;
import com.coopcredit.creditapplication.domain.ports.out.CreditApplicationRepositoryPort;
import com.coopcredit.creditapplication.domain.ports.out.MemberRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementación del caso de uso para eliminar un afiliado
 * 
 * Reglas de negocio:
 * - Solo se pueden eliminar afiliados que no tengan solicitudes de crédito
 * PENDIENTES o APPROVED
 * - Un afiliado con solicitudes REJECTED puede ser eliminado
 */
@Service
@Transactional
public class DeleteMemberUseCaseImpl implements DeleteMemberUseCase {

    private final MemberRepositoryPort memberRepository;
    private final CreditApplicationRepositoryPort creditApplicationRepository;

    public DeleteMemberUseCaseImpl(
            MemberRepositoryPort memberRepository,
            CreditApplicationRepositoryPort creditApplicationRepository) {
        this.memberRepository = memberRepository;
        this.creditApplicationRepository = creditApplicationRepository;
    }

    @Override
    public void execute(Long memberId) {
        // Verificar que el afiliado existe
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Member", "id", memberId));

        // Verificar que no tenga solicitudes activas (PENDING o APPROVED)
        List<CreditApplication> applications = creditApplicationRepository.findByMemberId(memberId);

        boolean hasActiveApplications = applications.stream()
                .anyMatch(app -> app.getStatus() == ApplicationStatus.PENDING ||
                        app.getStatus() == ApplicationStatus.APPROVED);

        if (hasActiveApplications) {
            throw new BusinessRuleException(
                    "MEMBER_HAS_ACTIVE_APPLICATIONS",
                    String.format("Cannot delete member %s (%s) - has active credit applications (PENDING or APPROVED)",
                            member.getName(), member.getDocument()));
        }

        // Eliminar el afiliado
        memberRepository.deleteById(memberId);
    }
}
