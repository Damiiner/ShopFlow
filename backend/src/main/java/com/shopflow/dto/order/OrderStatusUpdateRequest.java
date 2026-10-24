package com.shopflow.dto.order;

import com.shopflow.domain.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderStatusUpdateRequest {
    @NotNull
    private OrderStatus status;
}
