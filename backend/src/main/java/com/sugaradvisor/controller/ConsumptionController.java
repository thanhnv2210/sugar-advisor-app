package com.sugaradvisor.controller;

import com.sugaradvisor.dto.ConsumptionRequest;
import com.sugaradvisor.dto.ConsumptionResponse;
import com.sugaradvisor.service.ConsumptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

    @GetMapping("/{userId}")
    public Flux<ConsumptionResponse> getHistory(@PathVariable UUID userId) {
        return consumptionService.getHistory(userId).map(ConsumptionResponse::from);
    }
}
