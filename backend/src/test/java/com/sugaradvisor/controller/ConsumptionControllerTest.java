package com.sugaradvisor.controller;

import com.sugaradvisor.domain.Consumption;
import com.sugaradvisor.service.ConsumptionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@WebFluxTest(ConsumptionController.class)
class ConsumptionControllerTest {

    @Autowired
    WebTestClient webClient;

    @MockBean
    ConsumptionService consumptionService;

    private final UUID USER_ID = UUID.randomUUID();
    private final UUID CONSUMPTION_ID = UUID.randomUUID();

    private Consumption sampleConsumption() {
        return Consumption.builder()
                .id(CONSUMPTION_ID).userId(USER_ID)
                .sugarAmount(BigDecimal.valueOf(12.5))
                .consumedAt(LocalDateTime.now()).build();
    }

    @Test
    void record_validRequest_returns201WithBody() {
        when(consumptionService.record(any())).thenReturn(Mono.just(sampleConsumption()));

        webClient.post().uri("/api/consumptions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"userId\":\"" + USER_ID + "\",\"sugarAmount\":12.5}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.sugarAmount").isEqualTo(12.5)
                .jsonPath("$.userId").isEqualTo(USER_ID.toString());
    }

    @Test
    void record_missingUserId_returns400() {
        webClient.post().uri("/api/consumptions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"sugarAmount\":12.5}")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void record_negativeSugarAmount_returns400() {
        webClient.post().uri("/api/consumptions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"userId\":\"" + USER_ID + "\",\"sugarAmount\":-1}")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void delete_exists_returns204() {
        when(consumptionService.delete(CONSUMPTION_ID)).thenReturn(Mono.empty());

        webClient.delete().uri("/api/consumptions/{id}", CONSUMPTION_ID)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void delete_notFound_returns404() {
        when(consumptionService.delete(CONSUMPTION_ID))
                .thenReturn(Mono.error(new ConsumptionService.ConsumptionNotFoundException(CONSUMPTION_ID)));

        webClient.delete().uri("/api/consumptions/{id}", CONSUMPTION_ID)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getHistory_noParams_returns200WithDefaultPagination() {
        when(consumptionService.getHistory(eq(USER_ID), isNull(), isNull(), any()))
                .thenReturn(Flux.just(sampleConsumption()));

        webClient.get().uri("/api/consumptions/{userId}", USER_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].sugarAmount").isEqualTo(12.5)
                .jsonPath("$[0].userId").isEqualTo(USER_ID.toString());
    }

    @Test
    void getHistory_withDateRange_returnsFilteredResults() {
        LocalDate from = LocalDate.of(2026, 4, 1);
        LocalDate to = LocalDate.of(2026, 4, 23);
        when(consumptionService.getHistory(eq(USER_ID), eq(from), eq(to), any()))
                .thenReturn(Flux.just(sampleConsumption()));

        webClient.get().uri("/api/consumptions/{userId}?from=2026-04-01&to=2026-04-23", USER_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].sugarAmount").isEqualTo(12.5);
    }

    @Test
    void getHistory_withPagination_returns200() {
        when(consumptionService.getHistory(eq(USER_ID), isNull(), isNull(), any()))
                .thenReturn(Flux.just(sampleConsumption()));

        webClient.get().uri("/api/consumptions/{userId}?page=0&size=5", USER_ID)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getHistory_noRecords_returns200WithEmptyArray() {
        when(consumptionService.getHistory(eq(USER_ID), isNull(), isNull(), any()))
                .thenReturn(Flux.empty());

        webClient.get().uri("/api/consumptions/{userId}", USER_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isEmpty();
    }
}
