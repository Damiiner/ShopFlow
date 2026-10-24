package com.shopflow.dto.order;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class OrderItemResponse {
    Long productId;
    String productName;
    Long variantId;
    int quantity;
    BigDecimal unitPrice;
}
