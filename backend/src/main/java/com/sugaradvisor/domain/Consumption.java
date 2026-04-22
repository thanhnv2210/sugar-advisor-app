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
@Table("sugar_advisor.consumptions")
public class Consumption {

    @Id
    private UUID id;

    @Column("user_id")
    private UUID userId;

    @Column("family_member_id")
    private UUID familyMemberId;

    @Column("product_id")
    private UUID productId;

    @Column("sugar_amount")
    private BigDecimal sugarAmount;

    @Column("consumed_at")
    private LocalDateTime consumedAt;
}
