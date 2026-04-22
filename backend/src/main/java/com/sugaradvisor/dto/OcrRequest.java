package com.sugaradvisor.dto;

import jakarta.validation.constraints.NotBlank;

public record OcrRequest(@NotBlank String imageBase64) {}
