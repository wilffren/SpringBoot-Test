package com.coopcredit.creditapplication.domain.ports.in;

import com.coopcredit.creditapplication.domain.model.Member;

/**
 * Port for retrieving a single member use case.
 */
public interface GetMemberUseCase {
    
    Member execute(Long id);
    
    Member executeByDocument(String document);
}