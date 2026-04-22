package com.sugaradvisor.dto;

import com.sugaradvisor.domain.User;

import java.math.BigDecimal;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        Integer age,
        BigDecimal weight,
        BigDecimal dailySugarLimit
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getAge(),
                user.getWeight(),
                user.getDailySugarLimit()
        );
    }
}
