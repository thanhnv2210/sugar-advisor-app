package com.sugaradvisor.dto;

import java.util.List;

public record OcrResponse(
        String rawText,
        String ingredients,
        List<String> detectedSugarKeywords
) {}
