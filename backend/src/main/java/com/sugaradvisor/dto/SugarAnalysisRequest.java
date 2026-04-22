package com.sugaradvisor.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.UUID;

public record SugarAnalysisRequest(
        @NotNull UUID userId,
        UUID productId,
        @NotNull @PositiveOrZero BigDecimal sugarAmount,
        String ingredients
) {}
