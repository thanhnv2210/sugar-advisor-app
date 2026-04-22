package com.sugaradvisor.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sugaradvisor.domain.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Component
public class OpenFoodFactsClient {

    private static final String BASE_URL = "https://world.openfoodfacts.org/api/v2/product";
    private static final String SOURCE = "OPEN_FOOD_FACTS";

    private final WebClient webClient;

    public OpenFoodFactsClient(WebClient.Builder builder) {
        this.webClient = builder
                .baseUrl(BASE_URL)
                .defaultHeader("User-Agent", "SugarAdvisorApp/1.0")
                .build();
    }

    public Mono<Product> fetchByBarcode(String barcode) {
        return webClient.get()
                .uri("/{barcode}", barcode)
                .retrieve()
                .bodyToMono(OffResponse.class)
                .flatMap(response -> {
                    if (response.status() != 1 || response.product() == null) {
                        log.debug("OpenFoodFacts: product not found for barcode {}", barcode);
                        return Mono.empty();
                    }
                    Product product = mapToProduct(barcode, response.product());
                    log.debug("OpenFoodFacts: found product '{}' for barcode {}", product.getName(), barcode);
                    return Mono.just(product);
                })
                .onErrorResume(ex -> {
                    log.warn("OpenFoodFacts API error for barcode {}: {}", barcode, ex.getMessage());
                    return Mono.empty();
                });
    }

    private Product mapToProduct(String barcode, OffProduct p) {
        BigDecimal sugar = null;
        if (p.nutriments() != null) {
            Object raw = p.nutriments().get("sugars_100g");
            if (raw != null) {
                sugar = new BigDecimal(raw.toString());
            }
        }
        return Product.builder()
                .name(p.productName())
                .brand(p.brands())
                .barcode(barcode)
                .sugarPer100g(sugar)
                .ingredients(p.ingredientsText())
                .source(SOURCE)
                .build();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record OffResponse(int status, @JsonProperty("product") OffProduct product) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record OffProduct(
            @JsonProperty("product_name") String productName,
            String brands,
            @JsonProperty("ingredients_text") String ingredientsText,
            Map<String, Object> nutriments
    ) {}
}
