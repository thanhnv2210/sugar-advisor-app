package com.sugaradvisor.dto;

import com.sugaradvisor.domain.Consumption;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ConsumptionResponse(
        UUID id,
        UUID userId,
        UUID productId,
        UUID familyMemberId,
        BigDecimal sugarAmount,
        LocalDateTime consumedAt
) {
    public static ConsumptionResponse from(Consumption c) {
        return new ConsumptionResponse(
                c.getId(),
                c.getUserId(),
                c.getProductId(),
                c.getFamilyMemberId(),
                c.getSugarAmount(),
                c.getConsumedAt()
        );
    }
}
