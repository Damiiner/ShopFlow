package com.shopflow.dto.order;

import com.shopflow.domain.OrderStatus;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Value
@Builder
public class OrderResponse {
    Long id;
    String orderNumber;
    OrderStatus status;
    BigDecimal subtotal;
    BigDecimal shippingFee;
    BigDecimal discountAmount;
    BigDecimal totalTtc;
    Instant orderedAt;
    boolean statusNotificationNew;
    String shippingStreet;
    String shippingCity;
    String shippingPostalCode;
    String shippingCountry;
    List<OrderItemResponse> items;
}
