package com.sugaradvisor.dto;

import java.math.BigDecimal;

public record SugarAnalysisResponse(
        String sugarLevel,
        boolean isExceedLimit,
        BigDecimal remainingSugar,
        String recommendation
) {}
