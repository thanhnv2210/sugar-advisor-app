package com.sugaradvisor.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record UserUpdateRequest(
        String name,
        @Min(1) Integer age,
        @PositiveOrZero BigDecimal weight,
        @PositiveOrZero BigDecimal dailySugarLimit
) {}
