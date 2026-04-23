package com.sugaradvisor.controller;

import com.sugaradvisor.dto.DailySummaryResponse;
import com.sugaradvisor.dto.FamilyMemberRequest;
import com.sugaradvisor.dto.FamilyMemberResponse;
import com.sugaradvisor.service.ConsumptionService;
import com.sugaradvisor.service.FamilyMemberService;
import com.sugaradvisor.service.SummaryService;
import com.sugaradvisor.dto.ConsumptionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/users/{userId}/family")
@RequiredArgsConstructor
public class FamilyMemberController {

    private final FamilyMemberService familyMemberService;
    private final ConsumptionService consumptionService;
    private final SummaryService summaryService;

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

    @GetMapping("/{memberId}/consumptions")
    public Flux<ConsumptionResponse> getConsumptions(
            @PathVariable UUID userId,
            @PathVariable UUID memberId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return familyMemberService.getOne(userId, memberId)
                .thenMany(consumptionService.getHistoryByFamilyMember(memberId, from, to, PageRequest.of(page, size)))
                .map(ConsumptionResponse::from);
    }

    @GetMapping("/{memberId}/summary/today")
    public Mono<DailySummaryResponse> getSummaryToday(
            @PathVariable UUID userId,
            @PathVariable UUID memberId) {
        return summaryService.getFamilyMemberSummaryToday(userId, memberId);
    }
}
