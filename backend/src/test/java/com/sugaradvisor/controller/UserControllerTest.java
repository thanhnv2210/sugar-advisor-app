package com.sugaradvisor.controller;

import com.sugaradvisor.domain.User;
import com.sugaradvisor.dto.DailySummaryResponse;
import com.sugaradvisor.service.SummaryService;
import com.sugaradvisor.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebFluxTest(UserController.class)
class UserControllerTest {

    @Autowired
    WebTestClient webClient;

    @MockBean
    UserService userService;

    @MockBean
    SummaryService summaryService;

    private final UUID USER_ID = UUID.randomUUID();

    private User sampleUser() {
        return User.builder()
                .id(USER_ID).name("Alice").age(30)
                .dailySugarLimit(BigDecimal.valueOf(50)).build();
    }

    @Test
    void createUser_validRequest_returns201WithBody() {
        when(userService.createUser(any())).thenReturn(Mono.just(sampleUser()));

        webClient.post().uri("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Alice\",\"age\":30}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Alice")
                .jsonPath("$.id").isEqualTo(USER_ID.toString());
    }

    @Test
    void createUser_blankName_returns400() {
        webClient.post().uri("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"\"}")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void createUser_missingName_returns400() {
        webClient.post().uri("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"age\":30}")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void getUser_exists_returns200WithBody() {
        when(userService.getUser(USER_ID)).thenReturn(Mono.just(sampleUser()));

        webClient.get().uri("/api/users/{id}", USER_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(USER_ID.toString())
                .jsonPath("$.name").isEqualTo("Alice");
    }

    @Test
    void getUser_notFound_returns404() {
        when(userService.getUser(USER_ID))
                .thenReturn(Mono.error(new RuntimeException("User not found: " + USER_ID)));

        webClient.get().uri("/api/users/{id}", USER_ID)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void updateUser_validRequest_returns200() {
        when(userService.updateUser(eq(USER_ID), any())).thenReturn(Mono.just(sampleUser()));

        webClient.patch().uri("/api/users/{id}", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Bob\",\"dailySugarLimit\":40}")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void updateUser_notFound_returns404() {
        when(userService.updateUser(eq(USER_ID), any()))
                .thenReturn(Mono.error(new RuntimeException("User not found: " + USER_ID)));

        webClient.patch().uri("/api/users/{id}", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Bob\"}")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getSummaryToday_returns200WithAllFields() {
        DailySummaryResponse summary = new DailySummaryResponse(20.0, 50.0, 30.0, 2, "LOW");
        when(summaryService.getUserSummaryToday(USER_ID)).thenReturn(Mono.just(summary));

        webClient.get().uri("/api/users/{id}/summary/today", USER_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.totalSugar").isEqualTo(20.0)
                .jsonPath("$.dailyLimit").isEqualTo(50.0)
                .jsonPath("$.remaining").isEqualTo(30.0)
                .jsonPath("$.itemCount").isEqualTo(2)
                .jsonPath("$.sugarStatus").isEqualTo("LOW");
    }
}
