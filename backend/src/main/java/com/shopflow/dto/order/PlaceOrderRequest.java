package com.shopflow.dto.order;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PlaceOrderRequest {
    @NotNull
    private Long addressId;
}
