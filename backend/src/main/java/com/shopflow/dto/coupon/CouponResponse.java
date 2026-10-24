package com.shopflow.dto.coupon;

import com.shopflow.domain.CouponType;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;

@Value
@Builder
public class CouponResponse {
    Long id;
    String code;
    CouponType type;
    BigDecimal value;
    Instant expiresAt;
    Integer maxUses;
    int currentUses;
    boolean active;
}
