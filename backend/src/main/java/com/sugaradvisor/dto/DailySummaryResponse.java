package com.sugaradvisor.dto;

public record DailySummaryResponse(
        double totalSugar,
        double dailyLimit,
        double remaining,
        int itemCount,
        String sugarStatus   // LOW / MEDIUM / HIGH — how the whole day looks
) {}
