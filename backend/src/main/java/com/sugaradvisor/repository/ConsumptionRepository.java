package com.sugaradvisor.repository;

import com.sugaradvisor.domain.Consumption;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface ConsumptionRepository extends ReactiveCrudRepository<Consumption, UUID> {

    Flux<Consumption> findByUserIdOrderByConsumedAtDesc(UUID userId);

    Flux<Consumption> findByFamilyMemberIdOrderByConsumedAtDesc(UUID familyMemberId);

    @Query("SELECT COALESCE(SUM(sugar_amount), 0) FROM sugar_advisor.consumptions WHERE user_id = :userId AND consumed_at >= :startOfDay")
    Mono<Double> sumSugarTodayByUserId(UUID userId, LocalDateTime startOfDay);

    @Query("SELECT COALESCE(SUM(sugar_amount), 0) FROM sugar_advisor.consumptions WHERE family_member_id = :familyMemberId AND consumed_at >= :startOfDay")
    Mono<Double> sumSugarTodayByFamilyMemberId(UUID familyMemberId, LocalDateTime startOfDay);

    @Query("SELECT COUNT(*) FROM sugar_advisor.consumptions WHERE user_id = :userId AND consumed_at >= :startOfDay")
    Mono<Integer> countTodayByUserId(UUID userId, LocalDateTime startOfDay);

    @Query("SELECT COUNT(*) FROM sugar_advisor.consumptions WHERE family_member_id = :familyMemberId AND consumed_at >= :startOfDay")
    Mono<Integer> countTodayByFamilyMemberId(UUID familyMemberId, LocalDateTime startOfDay);
}
