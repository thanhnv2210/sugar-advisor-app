package com.sugaradvisor.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record FamilyMemberRequest(
        @NotBlank String name,
        @Min(1) Integer age,
        String relation,
        @PositiveOrZero BigDecimal dailySugarLimit
) {}
