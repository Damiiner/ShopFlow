package com.shopflow.dto.product;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class ProductSummaryResponse {
    Long id;
    String name;
    BigDecimal price;
    BigDecimal promoPrice;
    boolean onPromo;
    int discountPercent;
    String mainImageUrl;
    Double averageRating;
    String shopName;
    Long sellerId;
}
