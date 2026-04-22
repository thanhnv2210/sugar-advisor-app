package com.sugaradvisor.dto;

import com.sugaradvisor.domain.Product;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        String brand,
        String barcode,
        BigDecimal sugarPer100g,
        String ingredients
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getBrand(),
                product.getBarcode(),
                product.getSugarPer100g(),
                product.getIngredients()
        );
    }
}
