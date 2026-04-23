package com.sugaradvisor.controller;

import com.sugaradvisor.dto.ConsumptionRequest;
import com.sugaradvisor.dto.ConsumptionResponse;
import com.sugaradvisor.service.ConsumptionService;
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
@RequestMapping("/api/consumptions")
@RequiredArgsConstructor
public class ConsumptionController {

    private final ConsumptionService consumptionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ConsumptionResponse> record(@Valid @RequestBody ConsumptionRequest request) {
        return consumptionService.record(request).map(ConsumptionResponse::from);
    }

    @DeleteMapping("/{consumptionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable UUID consumptionId) {
        return consumptionService.delete(consumptionId);
    }

    @GetMapping("/{userId}")
    public Flux<ConsumptionResponse> getHistory(
            @PathVariable UUID userId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return consumptionService.getHistory(userId, from, to, PageRequest.of(page, size))
                .map(ConsumptionResponse::from);
    }
}
