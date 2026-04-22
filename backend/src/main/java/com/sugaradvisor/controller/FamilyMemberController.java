package com.sugaradvisor.controller;

import com.sugaradvisor.dto.FamilyMemberRequest;
import com.sugaradvisor.dto.FamilyMemberResponse;
import com.sugaradvisor.service.FamilyMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/users/{userId}/family")
@RequiredArgsConstructor
public class FamilyMemberController {

    private final FamilyMemberService familyMemberService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<FamilyMemberResponse> create(
            @PathVariable UUID userId,
            @Valid @RequestBody FamilyMemberRequest request) {
        return familyMemberService.create(userId, request).map(FamilyMemberResponse::from);
    }

    @GetMapping
    public Flux<FamilyMemberResponse> list(@PathVariable UUID userId) {
        return familyMemberService.listByUser(userId).map(FamilyMemberResponse::from);
    }

    @GetMapping("/{memberId}")
    public Mono<FamilyMemberResponse> getOne(
            @PathVariable UUID userId,
            @PathVariable UUID memberId) {
        return familyMemberService.getOne(userId, memberId).map(FamilyMemberResponse::from);
    }

    @PutMapping("/{memberId}")
    public Mono<FamilyMemberResponse> update(
            @PathVariable UUID userId,
            @PathVariable UUID memberId,
            @Valid @RequestBody FamilyMemberRequest request) {
        return familyMemberService.update(userId, memberId, request).map(FamilyMemberResponse::from);
    }

    @DeleteMapping("/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(
            @PathVariable UUID userId,
            @PathVariable UUID memberId) {
        return familyMemberService.delete(userId, memberId);
    }
}
