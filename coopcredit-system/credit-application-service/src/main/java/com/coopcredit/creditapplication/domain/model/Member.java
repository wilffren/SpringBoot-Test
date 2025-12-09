package com.coopcredit.creditapplication.domain.model;

import com.coopcredit.creditapplication.domain.model.enums.MemberStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Domain model representing a cooperative member (afiliado).
 */
public class Member {
    
    private Long id;
    private String document;
    private String name;
    private BigDecimal salary;
    private LocalDate affiliationDate;
    private MemberStatus status;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public Member() {}
    
    public Member(Long id, String document, String name, BigDecimal salary, 
                  LocalDate affiliationDate, MemberStatus status, Long userId) {
        this.id = id;
        this.document = document;
        this.name = name;
        this.salary = salary;
        this.affiliationDate = affiliationDate;
        this.status = status;
        this.userId = userId;
    }
    
    // Business methods
    public long getSeniorityInMonths() {
        if (affiliationDate == null) return 0;
        return ChronoUnit.MONTHS.between(affiliationDate, LocalDate.now());
    }
    
    public boolean isActive() {
        return MemberStatus.ACTIVE.equals(status);
    }
    
    public BigDecimal getMaxCreditAmount() {
        // Business rule: max credit = 4x salary
        return salary.multiply(BigDecimal.valueOf(4));
    }
    
    // Builder pattern
    public static MemberBuilder builder() {
        return new MemberBuilder();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getDocument() { return document; }
    public void setDocument(String document) { this.document = document; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public BigDecimal getSalary() { return salary; }
    public void setSalary(BigDecimal salary) { this.salary = salary; }
    
    public LocalDate getAffiliationDate() { return affiliationDate; }
    public void setAffiliationDate(LocalDate affiliationDate) { this.affiliationDate = affiliationDate; }
    
    public MemberStatus getStatus() { return status; }
    public void setStatus(MemberStatus status) { this.status = status; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public static class MemberBuilder {
        private Long id;
        private String document;
        private String name;
        private BigDecimal salary;
        private LocalDate affiliationDate;
        private MemberStatus status = MemberStatus.ACTIVE;
        private Long userId;
        
        public MemberBuilder id(Long id) { this.id = id; return this; }
        public MemberBuilder document(String document) { this.document = document; return this; }
        public MemberBuilder name(String name) { this.name = name; return this; }
        public MemberBuilder salary(BigDecimal salary) { this.salary = salary; return this; }
        public MemberBuilder affiliationDate(LocalDate affiliationDate) { this.affiliationDate = affiliationDate; return this; }
        public MemberBuilder status(MemberStatus status) { this.status = status; return this; }
        public MemberBuilder userId(Long userId) { this.userId = userId; return this; }
        
        public Member build() {
            return new Member(id, document, name, salary, affiliationDate, status, userId);
        }
    }
}