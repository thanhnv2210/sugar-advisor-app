package com.sugaradvisor.repository;

import com.sugaradvisor.domain.FamilyMember;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface FamilyMemberRepository extends ReactiveCrudRepository<FamilyMember, UUID> {

    Flux<FamilyMember> findByUserId(UUID userId);
}
