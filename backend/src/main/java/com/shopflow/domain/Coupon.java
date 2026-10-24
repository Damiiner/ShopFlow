package com.shopflow.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "coupons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(255)")
    private CouponType type;

    @Column(name = "coupon_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal value;

    private Instant expiresAt;

    private Integer maxUses;

    @Column(nullable = false)
    @Builder.Default
    private int currentUses = 0;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
