package com.shopflow.dto.cart;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class CartLineResponse {
    Long itemId;
    Long productId;
    String productName;
    Long variantId;
    String variantLabel;
    int quantity;
    int maxQuantity;
    BigDecimal unitPrice;
    BigDecimal lineTotal;
    String imageUrl;
}
