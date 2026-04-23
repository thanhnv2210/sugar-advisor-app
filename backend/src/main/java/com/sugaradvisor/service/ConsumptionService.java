package com.sugaradvisor.service;

import com.sugaradvisor.domain.Consumption;
import com.sugaradvisor.dto.ConsumptionRequest;
import com.sugaradvisor.repository.ConsumptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    public Mono<Void> delete(UUID consumptionId) {
        return consumptionRepository.findById(consumptionId)
                .switchIfEmpty(Mono.error(new ConsumptionNotFoundException(consumptionId)))
                .flatMap(c -> consumptionRepository.deleteById(consumptionId));
    }

    public Flux<Consumption> getHistory(UUID userId, LocalDate from, LocalDate to, Pageable pageable) {
        if (from != null && to != null) {
            return consumptionRepository.findByUserIdAndConsumedAtBetweenOrderByConsumedAtDesc(
                    userId, from.atStartOfDay(), to.atTime(23, 59, 59), pageable);
        }
        return consumptionRepository.findByUserIdOrderByConsumedAtDesc(userId, pageable);
    }

    public Flux<Consumption> getHistoryByFamilyMember(UUID familyMemberId, LocalDate from, LocalDate to, Pageable pageable) {
        if (from != null && to != null) {
            return consumptionRepository.findByFamilyMemberIdAndConsumedAtBetweenOrderByConsumedAtDesc(
                    familyMemberId, from.atStartOfDay(), to.atTime(23, 59, 59), pageable);
        }
        return consumptionRepository.findByFamilyMemberIdOrderByConsumedAtDesc(familyMemberId, pageable);
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

    public static class ConsumptionNotFoundException extends RuntimeException {
        public ConsumptionNotFoundException(UUID id) {
            super("Consumption not found: " + id);
        }
    }
}
