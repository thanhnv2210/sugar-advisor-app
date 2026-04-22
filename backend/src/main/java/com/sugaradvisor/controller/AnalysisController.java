package com.sugaradvisor.controller;

import com.sugaradvisor.dto.SugarAnalysisRequest;
import com.sugaradvisor.dto.SugarAnalysisResponse;
import com.sugaradvisor.service.SugarAnalysisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final SugarAnalysisService sugarAnalysisService;

    @PostMapping("/sugar")
    public Mono<SugarAnalysisResponse> analyze(@Valid @RequestBody SugarAnalysisRequest request) {
        return sugarAnalysisService.analyze(request);
    }
}
