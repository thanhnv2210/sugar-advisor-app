package com.sugaradvisor.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record UserCreateRequest(
        @NotBlank String name,
        @Min(1) Integer age,
        BigDecimal weight
) {}
