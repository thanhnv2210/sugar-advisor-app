package com.sugaradvisor.service;

import com.sugaradvisor.domain.Consumption;
import com.sugaradvisor.dto.ConsumptionRequest;
import com.sugaradvisor.repository.ConsumptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConsumptionService {

    private final ConsumptionRepository consumptionRepository;

    public Mono<Consumption> record(ConsumptionRequest request) {
        Consumption consumption = Consumption.builder()
                .userId(request.userId())
                .productId(request.productId())
                .familyMemberId(request.familyMemberId())
                .sugarAmount(request.sugarAmount())
                .consumedAt(LocalDateTime.now())
                .build();
        return consumptionRepository.save(consumption);
    }

    public Flux<Consumption> getHistory(UUID userId) {
        return consumptionRepository.findByUserIdOrderByConsumedAtDesc(userId);
    }

    public Flux<Consumption> getHistoryByFamilyMember(UUID familyMemberId) {
        return consumptionRepository.findByFamilyMemberIdOrderByConsumedAtDesc(familyMemberId);
    }

    public Mono<Double> getTodayTotal(UUID userId) {
        return consumptionRepository.sumSugarTodayByUserId(userId, startOfToday());
    }

    public Mono<Double> getTodayTotalByFamilyMember(UUID familyMemberId) {
        return consumptionRepository.sumSugarTodayByFamilyMemberId(familyMemberId, startOfToday());
    }

    public Mono<Integer> getTodayCount(UUID userId) {
        return consumptionRepository.countTodayByUserId(userId, startOfToday());
    }

    public Mono<Integer> getTodayCountByFamilyMember(UUID familyMemberId) {
        return consumptionRepository.countTodayByFamilyMemberId(familyMemberId, startOfToday());
    }

    private LocalDateTime startOfToday() {
        return LocalDate.now().atStartOfDay();
    }
}
