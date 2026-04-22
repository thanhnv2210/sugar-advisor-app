package com.sugaradvisor.service;

import com.sugaradvisor.client.OpenFoodFactsClient;
import com.sugaradvisor.domain.Product;
import com.sugaradvisor.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private static final String CACHE_PREFIX = "product:barcode:";
    private static final Duration CACHE_TTL = Duration.ofHours(24);

    private final ProductRepository productRepository;
    private final ReactiveRedisTemplate<String, Product> redisTemplate;
    private final OpenFoodFactsClient openFoodFactsClient;

    public Mono<Product> findByBarcode(String barcode) {
        String cacheKey = CACHE_PREFIX + barcode;

        return redisTemplate.opsForValue().get(cacheKey)
                .doOnNext(p -> log.debug("Cache hit for barcode {}", barcode))
                .switchIfEmpty(
                        productRepository.findByBarcode(barcode)
                                .doOnNext(p -> log.debug("DB hit for barcode {}", barcode))
                                .switchIfEmpty(fetchFromOpenFoodFacts(barcode))
                                .flatMap(p -> redisTemplate.opsForValue()
                                        .set(cacheKey, p, CACHE_TTL)
                                        .thenReturn(p))
                                .switchIfEmpty(Mono.error(new ProductNotFoundException(barcode)))
                );
    }

    private Mono<Product> fetchFromOpenFoodFacts(String barcode) {
        return openFoodFactsClient.fetchByBarcode(barcode)
                .flatMap(product -> {
                    log.debug("Saving OpenFoodFacts product to DB: {}", product.getName());
                    return productRepository.save(product);
                });
    }

    public static class ProductNotFoundException extends RuntimeException {
        public ProductNotFoundException(String barcode) {
            super("Product not found for barcode: " + barcode);
        }
    }
}
