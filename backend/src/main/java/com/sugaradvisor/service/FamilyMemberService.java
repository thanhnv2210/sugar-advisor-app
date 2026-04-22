package com.sugaradvisor.service;

import com.sugaradvisor.domain.FamilyMember;
import com.sugaradvisor.dto.FamilyMemberRequest;
import com.sugaradvisor.repository.FamilyMemberRepository;
import com.sugaradvisor.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FamilyMemberService {

    private final FamilyMemberRepository familyMemberRepository;
    private final UserRepository userRepository;

    public Mono<FamilyMember> create(UUID userId, FamilyMemberRequest request) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new RuntimeException("User not found: " + userId)))
                .flatMap(user -> {
                    FamilyMember member = FamilyMember.builder()
                            .userId(userId)
                            .name(request.name())
                            .age(request.age())
                            .relation(request.relation())
                            .dailySugarLimit(request.dailySugarLimit())
                            .build();
                    return familyMemberRepository.save(member);
                });
    }

    public Flux<FamilyMember> listByUser(UUID userId) {
        return familyMemberRepository.findByUserId(userId);
    }

    public Mono<FamilyMember> getOne(UUID userId, UUID memberId) {
        return familyMemberRepository.findById(memberId)
                .filter(m -> m.getUserId().equals(userId))
                .switchIfEmpty(Mono.error(new MemberNotFoundException(memberId)));
    }

    public Mono<FamilyMember> update(UUID userId, UUID memberId, FamilyMemberRequest request) {
        return getOne(userId, memberId)
                .flatMap(member -> {
                    if (request.name() != null) member.setName(request.name());
                    if (request.age() != null) member.setAge(request.age());
                    if (request.relation() != null) member.setRelation(request.relation());
                    if (request.dailySugarLimit() != null) member.setDailySugarLimit(request.dailySugarLimit());
                    return familyMemberRepository.save(member);
                });
    }

    public Mono<Void> delete(UUID userId, UUID memberId) {
        return getOne(userId, memberId)
                .flatMap(member -> familyMemberRepository.deleteById(memberId));
    }

    public static class MemberNotFoundException extends RuntimeException {
        public MemberNotFoundException(UUID memberId) {
            super("Family member not found: " + memberId);
        }
    }
}
