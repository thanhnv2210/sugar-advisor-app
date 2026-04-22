package com.sugaradvisor.controller;

import com.sugaradvisor.dto.ProductResponse;
import com.sugaradvisor.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/barcode/{barcode}")
    public Mono<ProductResponse> getByBarcode(@PathVariable String barcode) {
        return productService.findByBarcode(barcode).map(ProductResponse::from);
    }
}
