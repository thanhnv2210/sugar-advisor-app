package com.sugaradvisor.repository;

import com.sugaradvisor.domain.SugarAnalysis;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface SugarAnalysisRepository extends ReactiveCrudRepository<SugarAnalysis, UUID> {

    Flux<SugarAnalysis> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
