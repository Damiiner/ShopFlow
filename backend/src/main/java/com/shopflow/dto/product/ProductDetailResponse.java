package com.shopflow.dto.product;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Value
@Builder
public class ProductDetailResponse {
    Long id;
    String name;
    String description;
    BigDecimal price;
    BigDecimal promoPrice;
    boolean onPromo;
    int discountPercent;
    int stock;
    List<String> imageUrls;
    List<ProductVariantDto> variants;
    List<CategoryTinyDto> categories;
    Double averageRating;
    long reviewCount;
    String shopName;
    Long sellerId;
    Instant createdAt;
}
