package com.sugaradvisor.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("sugar_advisor.products")
public class Product {

    @Id
    private UUID id;

    private String name;

    private String brand;

    private String barcode;

    @Column("sugar_per_100g")
    private BigDecimal sugarPer100g;

    private String ingredients;

    private String source;
}
