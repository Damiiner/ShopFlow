package com.shopflow.dto.cart;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CouponApplyRequest {
    @NotBlank
    private String code;
}
