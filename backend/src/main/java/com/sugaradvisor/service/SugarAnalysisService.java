package com.sugaradvisor.service;

import com.sugaradvisor.domain.SugarAnalysis;
import com.sugaradvisor.dto.SugarAnalysisRequest;
import com.sugaradvisor.dto.SugarAnalysisResponse;
import com.sugaradvisor.repository.SugarAnalysisRepository;
import com.sugaradvisor.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SugarAnalysisService {

    private static final BigDecimal DEFAULT_DAILY_LIMIT = BigDecimal.valueOf(50.0);
    private static final BigDecimal LOW_THRESHOLD = BigDecimal.valueOf(5.0);
    private static final BigDecimal HIGH_THRESHOLD = BigDecimal.valueOf(15.0);

    private final UserRepository userRepository;
    private final ConsumptionService consumptionService;
    private final SugarAnalysisRepository sugarAnalysisRepository;

    public Mono<SugarAnalysisResponse> analyze(SugarAnalysisRequest request) {
        return userRepository.findById(request.userId())
                .switchIfEmpty(Mono.error(new RuntimeException("User not found: " + request.userId())))
                .flatMap(user -> {
                    BigDecimal dailyLimit = user.getDailySugarLimit() != null
                            ? user.getDailySugarLimit()
                            : DEFAULT_DAILY_LIMIT;

                    return consumptionService.getTodayTotal(request.userId())
                            .map(todayTotal -> {
                                BigDecimal consumed = BigDecimal.valueOf(todayTotal);
                                BigDecimal remaining = dailyLimit.subtract(consumed);
                                BigDecimal sugarAmount = request.sugarAmount();

                                String sugarLevel = determineSugarLevel(sugarAmount);
                                boolean isExceedLimit = sugarAmount.compareTo(remaining) > 0;
                                BigDecimal remainingAfter = remaining.subtract(sugarAmount);
                                String recommendation = buildRecommendation(sugarLevel, isExceedLimit, remainingAfter);

                                return new AnalysisResult(sugarLevel, isExceedLimit, remainingAfter, recommendation);
                            });
                })
                .flatMap(result -> {
                    SugarAnalysis analysis = SugarAnalysis.builder()
                            .userId(request.userId())
                            .productId(request.productId())
                            .sugarAmount(request.sugarAmount())
                            .sugarLevel(result.sugarLevel())
                            .isExceedLimit(result.isExceedLimit())
                            .remainingSugar(result.remainingSugar())
                            .recommendation(result.recommendation())
                            .createdAt(LocalDateTime.now())
                            .build();
                    return sugarAnalysisRepository.save(analysis);
                })
                .map(saved -> new SugarAnalysisResponse(
                        saved.getSugarLevel(),
                        saved.getIsExceedLimit(),
                        saved.getRemainingSugar(),
                        saved.getRecommendation()
                ));
    }

    private String determineSugarLevel(BigDecimal sugarAmount) {
        if (sugarAmount.compareTo(LOW_THRESHOLD) < 0) return "LOW";
        if (sugarAmount.compareTo(HIGH_THRESHOLD) < 0) return "MEDIUM";
        return "HIGH";
    }

    private String buildRecommendation(String sugarLevel, boolean isExceedLimit, BigDecimal remainingAfter) {
        if (isExceedLimit) {
            return "This will exceed your daily sugar limit. Consider a smaller portion or skip this item.";
        }
        return switch (sugarLevel) {
            case "LOW" -> String.format("Great choice! Low sugar. You'll have %.1fg remaining today.", remainingAfter);
            case "MEDIUM" -> String.format("Moderate sugar. You'll have %.1fg remaining today.", remainingAfter);
            case "HIGH" -> String.format("High sugar content. Only %.1fg left in your daily budget.", remainingAfter);
            default -> "Track this item to stay within your daily sugar budget.";
        };
    }

    private record AnalysisResult(String sugarLevel, boolean isExceedLimit, BigDecimal remainingSugar, String recommendation) {}
}
