package com.sugaradvisor.controller;

import com.sugaradvisor.domain.Consumption;
import com.sugaradvisor.domain.FamilyMember;
import com.sugaradvisor.dto.DailySummaryResponse;
import com.sugaradvisor.service.ConsumptionService;
import com.sugaradvisor.service.FamilyMemberService;
import com.sugaradvisor.service.SummaryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@WebFluxTest(FamilyMemberController.class)
class FamilyMemberControllerTest {

    @Autowired
    WebTestClient webClient;

    @MockBean
    FamilyMemberService familyMemberService;

    @MockBean
    ConsumptionService consumptionService;

    @MockBean
    SummaryService summaryService;

    private final UUID USER_ID = UUID.randomUUID();
    private final UUID MEMBER_ID = UUID.randomUUID();

    private FamilyMember sampleMember() {
        return FamilyMember.builder()
                .id(MEMBER_ID).userId(USER_ID).name("Child").age(5)
                .relation("child").dailySugarLimit(BigDecimal.valueOf(25)).build();
    }

    @Test
    void createMember_validRequest_returns201() {
        when(familyMemberService.create(eq(USER_ID), any())).thenReturn(Mono.just(sampleMember()));

        webClient.post().uri("/api/users/{userId}/family", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Child\",\"age\":5,\"relation\":\"child\",\"dailySugarLimit\":25}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Child")
                .jsonPath("$.userId").isEqualTo(USER_ID.toString());
    }

    @Test
    void createMember_blankName_returns400() {
        webClient.post().uri("/api/users/{userId}/family", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"\"}")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void listMembers_returns200WithArray() {
        when(familyMemberService.listByUser(USER_ID)).thenReturn(Flux.just(sampleMember()));

        webClient.get().uri("/api/users/{userId}/family", USER_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].name").isEqualTo("Child")
                .jsonPath("$[0].id").isEqualTo(MEMBER_ID.toString());
    }

    @Test
    void getMember_exists_returns200() {
        when(familyMemberService.getOne(USER_ID, MEMBER_ID)).thenReturn(Mono.just(sampleMember()));

        webClient.get().uri("/api/users/{userId}/family/{memberId}", USER_ID, MEMBER_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(MEMBER_ID.toString());
    }

    @Test
    void getMember_notFound_returns404() {
        when(familyMemberService.getOne(USER_ID, MEMBER_ID))
                .thenReturn(Mono.error(new FamilyMemberService.MemberNotFoundException(MEMBER_ID)));

        webClient.get().uri("/api/users/{userId}/family/{memberId}", USER_ID, MEMBER_ID)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void updateMember_validRequest_returns200() {
        when(familyMemberService.update(eq(USER_ID), eq(MEMBER_ID), any()))
                .thenReturn(Mono.just(sampleMember()));

        webClient.put().uri("/api/users/{userId}/family/{memberId}", USER_ID, MEMBER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Updated\",\"age\":6,\"relation\":\"child\",\"dailySugarLimit\":25}")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void deleteMember_returns204() {
        when(familyMemberService.delete(USER_ID, MEMBER_ID)).thenReturn(Mono.empty());

        webClient.delete().uri("/api/users/{userId}/family/{memberId}", USER_ID, MEMBER_ID)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void getConsumptions_returns200WithHistory() {
        Consumption c = Consumption.builder()
                .id(UUID.randomUUID()).userId(USER_ID).familyMemberId(MEMBER_ID)
                .sugarAmount(BigDecimal.valueOf(8)).consumedAt(LocalDateTime.now()).build();
        when(familyMemberService.getOne(USER_ID, MEMBER_ID)).thenReturn(Mono.just(sampleMember()));
        when(consumptionService.getHistoryByFamilyMember(eq(MEMBER_ID), isNull(), isNull(), any())).thenReturn(Flux.just(c));

        webClient.get().uri("/api/users/{userId}/family/{memberId}/consumptions", USER_ID, MEMBER_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].sugarAmount").isEqualTo(8);
    }

    @Test
    void getFamilyMemberSummaryToday_returns200WithAllFields() {
        DailySummaryResponse summary = new DailySummaryResponse(8.0, 25.0, 17.0, 1, "LOW");
        when(summaryService.getFamilyMemberSummaryToday(USER_ID, MEMBER_ID)).thenReturn(Mono.just(summary));

        webClient.get().uri("/api/users/{userId}/family/{memberId}/summary/today", USER_ID, MEMBER_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.totalSugar").isEqualTo(8.0)
                .jsonPath("$.dailyLimit").isEqualTo(25.0)
                .jsonPath("$.sugarStatus").isEqualTo("LOW");
    }
}
