package com.sugaradvisor.controller;

import com.sugaradvisor.dto.OcrRequest;
import com.sugaradvisor.dto.OcrResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * OCR endpoint — provider pending (see Pending.md).
 * Currently returns a stub response.
 */
@RestController
@RequestMapping("/api/scan")
public class ScanController {

    @PostMapping("/ocr")
    public Mono<OcrResponse> extractFromImage(@Valid @RequestBody OcrRequest request) {
        // TODO: integrate OCR provider once decided (see Pending.md)
        OcrResponse stub = new OcrResponse(
                "[OCR provider pending]",
                null,
                List.of()
        );
        return Mono.just(stub);
    }
}
