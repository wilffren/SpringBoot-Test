package com.coopcredit.creditapplication.domain.ports.in;

/**
 * Use case para eliminar un afiliado
 * Solo usuarios con rol ADMIN pueden ejecutar esta operación
 */
public interface DeleteMemberUseCase {

    /**
     * Elimina un afiliado por su ID
     * 
     * @param memberId ID del afiliado a eliminar
     * @throws com.coopcredit.creditapplication.domain.exception.NotFoundException     si
     *                                                                                 el
     *                                                                                 afiliado
     *                                                                                 no
     *                                                                                 existe
     * @throws com.coopcredit.creditapplication.domain.exception.BusinessRuleException si
     *                                                                                 el
     *                                                                                 afiliado
     *                                                                                 tiene
     *                                                                                 solicitudes
     *                                                                                 de
     *                                                                                 crédito
     *                                                                                 activas
     */
    void execute(Long memberId);
}
