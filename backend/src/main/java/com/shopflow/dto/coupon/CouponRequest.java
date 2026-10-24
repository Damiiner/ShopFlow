package com.shopflow.dto.coupon;

import com.shopflow.domain.CouponType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class CouponRequest {
    @NotBlank
    private String code;

    @NotNull
    private CouponType type;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal value;

    private Instant expiresAt;
    private Integer maxUses;
    private boolean active = true;
}
