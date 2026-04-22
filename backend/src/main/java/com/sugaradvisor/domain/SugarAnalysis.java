package com.sugaradvisor.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("sugar_advisor.sugar_analysis")
public class SugarAnalysis {

    @Id
    private UUID id;

    @Column("user_id")
    private UUID userId;

    @Column("product_id")
    private UUID productId;

    @Column("sugar_amount")
    private BigDecimal sugarAmount;

    @Column("sugar_level")
    private String sugarLevel;

    @Column("is_exceed_limit")
    private Boolean isExceedLimit;

    @Column("remaining_sugar")
    private BigDecimal remainingSugar;

    private String recommendation;

    @Column("created_at")
    private LocalDateTime createdAt;
}
