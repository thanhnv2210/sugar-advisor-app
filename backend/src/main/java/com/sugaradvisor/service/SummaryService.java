package com.sugaradvisor.service;

import com.sugaradvisor.dto.DailySummaryResponse;
import com.sugaradvisor.repository.FamilyMemberRepository;
import com.sugaradvisor.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SummaryService {

    private static final double DEFAULT_DAILY_LIMIT = 50.0;

    private final UserRepository userRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final ConsumptionService consumptionService;

    public Mono<DailySummaryResponse> getUserSummaryToday(UUID userId) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new RuntimeException("User not found: " + userId)))
                .flatMap(user -> {
                    double limit = user.getDailySugarLimit() != null
                            ? user.getDailySugarLimit().doubleValue()
                            : DEFAULT_DAILY_LIMIT;
                    return Mono.zip(
                            consumptionService.getTodayTotal(userId),
                            consumptionService.getTodayCount(userId)
                    ).map(tuple -> buildSummary(tuple.getT1(), tuple.getT2(), limit));
                });
    }

    public Mono<DailySummaryResponse> getFamilyMemberSummaryToday(UUID userId, UUID memberId) {
        return familyMemberRepository.findById(memberId)
                .filter(m -> m.getUserId().equals(userId))
                .switchIfEmpty(Mono.error(new FamilyMemberService.MemberNotFoundException(memberId)))
                .flatMap(member -> {
                    double limit = member.getDailySugarLimit() != null
                            ? member.getDailySugarLimit().doubleValue()
                            : DEFAULT_DAILY_LIMIT;
                    return Mono.zip(
                            consumptionService.getTodayTotalByFamilyMember(memberId),
                            consumptionService.getTodayCountByFamilyMember(memberId)
                    ).map(tuple -> buildSummary(tuple.getT1(), tuple.getT2(), limit));
                });
    }

    private DailySummaryResponse buildSummary(double total, int count, double limit) {
        double remaining = Math.max(limit - total, 0);
        String status = sugarStatus(total, limit);
        return new DailySummaryResponse(total, limit, remaining, count, status);
    }

    private String sugarStatus(double total, double limit) {
        double ratio = total / limit;
        if (ratio < 0.5) return "LOW";
        if (ratio < 0.8) return "MEDIUM";
        return "HIGH";
    }
}
