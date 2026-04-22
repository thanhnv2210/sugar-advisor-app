package com.sugaradvisor.dto;

import com.sugaradvisor.domain.FamilyMember;

import java.math.BigDecimal;
import java.util.UUID;

public record FamilyMemberResponse(
        UUID id,
        UUID userId,
        String name,
        Integer age,
        String relation,
        BigDecimal dailySugarLimit
) {
    public static FamilyMemberResponse from(FamilyMember m) {
        return new FamilyMemberResponse(
                m.getId(),
                m.getUserId(),
                m.getName(),
                m.getAge(),
                m.getRelation(),
                m.getDailySugarLimit()
        );
    }
}
