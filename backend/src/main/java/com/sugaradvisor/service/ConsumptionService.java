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

    public Mono<Double> getTodayTotal(UUID userId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        return consumptionRepository.sumSugarTodayByUserId(userId, startOfDay);
    }
}
