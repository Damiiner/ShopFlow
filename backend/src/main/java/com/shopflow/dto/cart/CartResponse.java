package com.shopflow.dto.cart;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.List;

@Value
@Builder
public class CartResponse {
    Long cartId;
    List<CartLineResponse> lines;
    BigDecimal subtotal;
    BigDecimal discount;
    BigDecimal shippingFee;
    BigDecimal totalTtc;
    String couponCode;
}
