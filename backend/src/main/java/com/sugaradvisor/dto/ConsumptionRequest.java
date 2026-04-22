package com.sugaradvisor.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.UUID;

public record ConsumptionRequest(
        @NotNull UUID userId,
        UUID productId,
        UUID familyMemberId,
        @NotNull @PositiveOrZero BigDecimal sugarAmount
) {}
