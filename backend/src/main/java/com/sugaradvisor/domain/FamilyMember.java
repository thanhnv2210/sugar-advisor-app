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
@Table("sugar_advisor.family_members")
public class FamilyMember {

    @Id
    private UUID id;

    @Column("user_id")
    private UUID userId;

    private String name;

    private Integer age;

    private String relation;

    @Column("daily_sugar_limit")
    private BigDecimal dailySugarLimit;

    @Column("created_at")
    private LocalDateTime createdAt;
}
